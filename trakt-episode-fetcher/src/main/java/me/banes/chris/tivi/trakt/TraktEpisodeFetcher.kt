/*
 * Copyright 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.banes.chris.tivi.trakt

import com.uwetrottmann.trakt5.TraktV2
import com.uwetrottmann.trakt5.enums.Extended
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.daos.EntityInserter
import me.banes.chris.tivi.data.daos.EpisodesDao
import me.banes.chris.tivi.data.daos.SeasonsDao
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.Episode
import me.banes.chris.tivi.data.entities.Season
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.extensions.toRxMaybe
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.RetryAfterTimeoutWithDelay
import org.threeten.bp.OffsetDateTime
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import com.uwetrottmann.trakt5.entities.Episode as TraktEpisode

@Singleton
class TraktEpisodeFetcher @Inject constructor(
    private val showDao: TiviShowDao,
    private val seasonDao: SeasonsDao,
    private val episodesDao: EpisodesDao,
    private val trakt: TraktV2,
    private val schedulers: AppRxSchedulers,
    private val entityInserter: EntityInserter
) {
    fun loadShowSeasonEpisodes(seasonId: Long): Maybe<List<Episode>> {
        val dbSource = episodesDao.episodesFromSeasonId(seasonId)
                .subscribeOn(schedulers.database)
                .filter { it.isNotEmpty() }

        val networkSource = seasonDao.seasonWithId(seasonId)
                .subscribeOn(schedulers.database)
                .flatMap { season ->
                    showDao.getShowWithIdMaybe(season.showId!!)
                            .subscribeOn(schedulers.database)
                            .flatMap { show ->
                                fetchEpisodesFromTrakt(show, season)
                            }
                }

        return Maybe.concat(dbSource, networkSource).firstElement()
    }

    private fun shouldRetry(throwable: Throwable): Boolean = when (throwable) {
        is HttpException -> throwable.code() == 429
        is IOException -> true
        else -> false
    }

    private fun upsertEpisode(seasonId: Long, traktEpisode: TraktEpisode): Single<Episode> {
        return episodesDao.episodeWithTraktId(traktEpisode.ids.trakt)
                .defaultIfEmpty(Episode())
                .subscribeOn(schedulers.database)
                .map {
                    it.apply {
                        updateProperty(this::seasonId, seasonId)
                        updateProperty(this::traktId, traktEpisode.ids.trakt)
                        updateProperty(this::tmdbId, traktEpisode.ids.tmdb)
                        updateProperty(this::title, traktEpisode.title)
                        updateProperty(this::number, traktEpisode.number)
                        updateProperty(this::summary, traktEpisode.overview)
                        updateProperty(this::firstAired, traktEpisode.first_aired)
                        updateProperty(this::rating, traktEpisode.rating?.toFloat())
                        updateProperty(this::votes, traktEpisode.votes)
                        lastTraktUpdate = OffsetDateTime.now()
                    }
                    entityInserter.insertOrUpdate(episodesDao, it)
                }
                .flatMap(episodesDao::episodeWithId)
                .toSingle()
    }

    private fun fetchEpisodesFromTrakt(show: TiviShow, season: Season): Maybe<List<Episode>> {
        return trakt.seasons().season(show.traktId.toString(), season.number!!, Extended.EPISODES).toRxMaybe()
                .subscribeOn(schedulers.network)
                .retryWhen(RetryAfterTimeoutWithDelay(3, 1000, this::shouldRetry, schedulers.network))
                .toFlowable()
                .flatMapIterable { it }
                .flatMapSingle { upsertEpisode(season.id!!, it) }
                .toList()
                .toMaybe()
    }
}