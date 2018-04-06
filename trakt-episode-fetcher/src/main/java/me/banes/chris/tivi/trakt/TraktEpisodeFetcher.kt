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
import com.uwetrottmann.trakt5.entities.Episode as TraktEpisode
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.daos.EntityInserter
import me.banes.chris.tivi.data.daos.SeasonsDao
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.Episode
import me.banes.chris.tivi.data.entities.Season
import me.banes.chris.tivi.extensions.toRxMaybe
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.RetryAfterTimeoutWithDelay
import org.threeten.bp.OffsetDateTime
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import com.uwetrottmann.trakt5.entities.Season as TraktSeason

@Singleton
class TraktEpisodeFetcher @Inject constructor(
    private val showDao: TiviShowDao,
    private val seasonDao: SeasonsDao,
    private val trakt: TraktV2,
    private val schedulers: AppRxSchedulers,
    private val entityInserter: EntityInserter
) {
    fun loadShowSeasonEpisodes(showTraktId: Int, season: Int, episode: Int): Maybe<Episode>> {
        val dbSource = showDao.getShowWithTraktIdMaybe(showTraktId)
                .subscribeOn(schedulers.database)
                .observeOn(schedulers.database)
                .flatMap { seasonDao.seasonsForShowId(it.id!!) }

        val networkSource = trakt.episodes().summary(showTraktId.toString(), season, episode, Extended.NOSEASONS).toRxMaybe()
                        .subscribeOn(schedulers.network)
                        .retryWhen(RetryAfterTimeoutWithDelay(3, 1000, this::shouldRetry, schedulers.network))
                        .flatMapSingle(this::upsertSeason)
                        .toMaybe()

        return Maybe.concat(dbSource, networkSource).firstElement()
    }

    private fun shouldRetry(throwable: Throwable): Boolean = when (throwable) {
        is HttpException -> throwable.code() == 429
        is IOException -> true
        else -> false
    }

    private fun upsertSeason(traktEpisode: TraktEpisode): Single<Episode> {
        // TODO check that the season trakt id == show trakt id
        return seasonDao.seasonWithShowId(traktEpisode.ids.trakt, traktEpisode.number)
                .subscribeOn(schedulers.database)
                .map {
                    it.apply {
                        updateProperty(this::showId, showId)
                        updateProperty(this::traktId, traktEpisode.ids.trakt)
                        updateProperty(this::tmdbId, traktEpisode.ids.tmdb)
                        updateProperty(this::number, traktEpisode.number)
                        updateProperty(this::summary, traktEpisode.overview)
                        updateProperty(this::rating, traktEpisode.rating?.toFloat())
                        updateProperty(this::votes, traktEpisode.votes)
                        updateProperty(this::episodeCount, traktEpisode.episode_count)
                        updateProperty(this::airedEpisodes, traktEpisode.aired_episodes)
                        lastTraktUpdate = OffsetDateTime.now()
                    }
                    entityInserter.insertOrUpdate(seasonDao, it)
                }
                .flatMap(seasonDao::seasonWithId)
                .toSingle()
    }
}