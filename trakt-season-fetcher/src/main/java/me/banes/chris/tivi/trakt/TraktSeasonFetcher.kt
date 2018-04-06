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
import me.banes.chris.tivi.data.daos.SeasonsDao
import me.banes.chris.tivi.data.daos.TiviShowDao
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
class TraktSeasonFetcher @Inject constructor(
    private val showDao: TiviShowDao,
    private val seasonDao: SeasonsDao,
    private val trakt: TraktV2,
    private val schedulers: AppRxSchedulers,
    private val entityInserter: EntityInserter
) {
    fun loadShowSeasons(showTraktId: Int): Maybe<List<Season>> {
        val dbSource = showDao.getShowWithTraktIdMaybe(showTraktId)
                .subscribeOn(schedulers.database)
                .observeOn(schedulers.database)
                .flatMap { seasonDao.seasonsForShowId(it.id!!) }

        val networkSource = trakt.seasons().summary(showTraktId.toString(), Extended.FULL).toRxMaybe()
                        .subscribeOn(schedulers.network)
                        .retryWhen(RetryAfterTimeoutWithDelay(3, 1000, this::shouldRetry, schedulers.network))
                        .toFlowable()
                        .flatMapIterable { it }
                        .flatMapSingle(this::upsertSeason)
                        .toList()
                        .toMaybe()

        return Maybe.concat(dbSource, networkSource).firstElement()
    }

    private fun shouldRetry(throwable: Throwable): Boolean = when (throwable) {
        is HttpException -> throwable.code() == 429
        is IOException -> true
        else -> false
    }

    private fun upsertSeason(traktSeason: TraktSeason): Single<Season> {
        return seasonDao.seasonWithSeasonTraktId(traktSeason.ids.trakt)
                .defaultIfEmpty(Season())
                .subscribeOn(schedulers.database)
                .map {
                    it.apply {
                        updateProperty(this::showId, showId)
                        updateProperty(this::traktId, traktSeason.ids.trakt)
                        updateProperty(this::tmdbId, traktSeason.ids.tmdb)
                        updateProperty(this::number, traktSeason.number)
                        updateProperty(this::summary, traktSeason.overview)
                        updateProperty(this::rating, traktSeason.rating?.toFloat())
                        updateProperty(this::votes, traktSeason.votes)
                        updateProperty(this::episodeCount, traktSeason.episode_count)
                        updateProperty(this::airedEpisodes, traktSeason.aired_episodes)
                        lastTraktUpdate = OffsetDateTime.now()
                    }
                    entityInserter.insertOrUpdate(seasonDao, it)
                }
                .flatMap(seasonDao::seasonWithId)
                .toSingle()
    }
}