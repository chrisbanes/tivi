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

package app.tivi.trakt

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.Season
import app.tivi.extensions.fetchBodyWithRetry
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Seasons
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import com.uwetrottmann.trakt5.entities.Season as TraktSeason

@Singleton
class TraktSeasonFetcher @Inject constructor(
    private val showDao: TiviShowDao,
    private val seasonDao: SeasonsDao,
    private val seasonsService: Provider<Seasons>,
    private val entityInserter: EntityInserter,
    private val transactionRunner: DatabaseTransactionRunner,
    private val traktEpisodeFetcher: TraktEpisodeFetcher
) {
    suspend fun updateSeasonData(showId: Long) {
        val show = showDao.getShowWithId(showId)
                ?: throw IllegalArgumentException("Show with id[$showId] does not exist")

        val response = seasonsService.get().summary(show.traktId!!.toString(), Extended.FULLEPISODES)
                .fetchBodyWithRetry()

        transactionRunner.runInTransaction {
            response.forEach { traktSeason ->
                // Upsert the season
                val seasonId = upsertSeason(showId, traktSeason)
                // Now upsert all the episodes
                traktSeason.episodes?.forEach {
                    traktEpisodeFetcher.upsertEpisode(seasonId, it)
                }
            }
        }
    }

    private fun upsertSeason(showId: Long, traktSeason: TraktSeason): Long {
        return (seasonDao.seasonWithSeasonTraktId(traktSeason.ids.trakt) ?: Season(showId = showId)).copy(
                traktId = traktSeason.ids.trakt,
                tmdbId = traktSeason.ids.tmdb,
                number = traktSeason.number,
                title = traktSeason.title,
                summary = traktSeason.overview,
                rating = traktSeason.rating?.toFloat(),
                votes = traktSeason.votes,
                episodeCount = traktSeason.episode_count,
                airedEpisodes = traktSeason.aired_episodes,
                network = traktSeason.network
        ).let {
            entityInserter.insertOrUpdate(seasonDao, it)
        }
    }
}