/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.data.repositories.episodes

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import io.reactivex.Flowable
import javax.inject.Inject

class LocalSeasonsEpisodesStore @Inject constructor(
    private val entityInserter: EntityInserter,
    private val transactionRunner: DatabaseTransactionRunner,
    private val seasonsDao: SeasonsDao,
    private val episodesDao: EpisodesDao,
    private val episodeWatchEntryDao: EpisodeWatchEntryDao
) {
    fun observeEpisode(episodeId: Long): Flowable<Episode> {
        return episodesDao.episodeWithIdFlowable(episodeId)
    }

    fun observeShowSeasonsWithEpisodes(showId: Long): Flowable<List<SeasonWithEpisodesAndWatches>> {
        return seasonsDao.seasonsWithEpisodesForShowId(showId)
    }

    /**
     * Gets the ID for the season with the given trakt Id. If the trakt Id does not exist in the
     * database, it is inserted and the generated ID is returned.
     */
    fun getSeasonIdOrSavePlaceholder(season: Season): Long = transactionRunner {
        val seasonForTraktId = season.traktId?.let { seasonsDao.seasonWithSeasonTraktId(it) }
        seasonForTraktId?.id ?: seasonsDao.insert(season)
    }

    fun getSeason(id: Long) = seasonsDao.seasonWithId(id)

    fun getSeasonWithTraktId(traktId: Int) = seasonsDao.seasonWithSeasonTraktId(traktId)

    fun getEpisode(id: Long) = episodesDao.episodeWithId(id)

    fun getEpisodeWithTraktId(traktId: Int) = episodesDao.episodeWithTraktId(traktId)

    fun save(episode: Episode) = entityInserter.insertOrUpdate(episodesDao, episode)

    fun save(data: List<Pair<Season, List<Episode>>>) = transactionRunner {
        data.forEach { (season, episodes) ->
            val seasonId = entityInserter.insertOrUpdate(seasonsDao, season)
            episodes.forEach {
                entityInserter.insertOrUpdate(episodesDao, it.copy(seasonId = seasonId))
            }
        }
    }
}