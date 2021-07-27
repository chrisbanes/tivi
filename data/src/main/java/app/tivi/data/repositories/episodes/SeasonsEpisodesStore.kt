/*
 * Copyright 2019 Google LLC
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
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.resultentities.EpisodeWithSeason
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.data.syncers.syncerForEntity
import app.tivi.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class SeasonsEpisodesStore @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val seasonsDao: SeasonsDao,
    private val episodesDao: EpisodesDao,
    private val logger: Logger
) {
    private val seasonSyncer = syncerForEntity(
        seasonsDao,
        { it.traktId },
        { entity, id -> entity.copy(id = id ?: 0) },
        logger
    )

    private val episodeSyncer = syncerForEntity(
        episodesDao,
        { it.traktId },
        { entity, id -> entity.copy(id = id ?: 0) },
        logger
    )

    fun observeEpisode(episodeId: Long): Flow<EpisodeWithSeason> {
        return episodesDao.episodeWithIdObservable(episodeId)
    }

    fun observeShowSeasons(showId: Long): Flow<List<Season>> {
        return seasonsDao.observeSeasonsForShowId(showId)
    }

    fun observeShowSeasonsWithEpisodes(showId: Long): Flow<List<SeasonWithEpisodesAndWatches>> {
        return seasonsDao.seasonsWithEpisodesForShowId(showId)
    }

    fun observeShowSeasonWithEpisodes(seasonId: Long): Flow<SeasonWithEpisodesAndWatches> {
        return seasonsDao.seasonWithEpisodes(seasonId)
    }

    fun observeShowNextEpisodeToWatch(showId: Long): Flow<EpisodeWithSeason?> {
        return episodesDao.observeLatestWatchedEpisodeForShowId(showId).flatMapLatest {
            episodesDao.observeNextAiredEpisodeForShowAfter(
                showId,
                it?.season?.number ?: 0,
                it?.episode?.number ?: 0
            )
        }
    }

    /**
     * Gets the ID for the season with the given trakt Id. If the trakt Id does not exist in the
     * database, it is inserted and the generated ID is returned.
     */
    suspend fun getEpisodeIdForTraktId(traktId: Int): Long? {
        return episodesDao.episodeIdWithTraktId(traktId)
    }

    suspend fun getSeason(id: Long) = seasonsDao.seasonWithId(id)

    suspend fun getSeasonWithTraktId(traktId: Int) = seasonsDao.seasonWithTraktId(traktId)

    suspend fun getEpisodesInSeason(seasonId: Long) = episodesDao.episodesWithSeasonId(seasonId)

    suspend fun getEpisode(id: Long) = episodesDao.episodeWithId(id)

    suspend fun getEpisodeWithTraktId(traktId: Int) = episodesDao.episodeWithTraktId(traktId)

    suspend fun updateSeasonFollowed(seasonId: Long, followed: Boolean) {
        seasonsDao.updateSeasonIgnoreFlag(seasonId, !followed)
    }

    suspend fun updatePreviousSeasonFollowed(
        seasonId: Long,
        followed: Boolean
    ) = transactionRunner {
        for (id in seasonsDao.showPreviousSeasonIds(seasonId)) {
            seasonsDao.updateSeasonIgnoreFlag(id, !followed)
        }
    }

    suspend fun save(episode: Episode) = episodesDao.insertOrUpdate(episode)

    suspend fun save(showId: Long, data: Map<Season, List<Episode>>) = transactionRunner {
        seasonSyncer.sync(seasonsDao.seasonsForShowId(showId), data.keys)
        data.forEach { (season, episodes) ->
            val seasonId = seasonsDao.seasonWithTraktId(season.traktId!!)!!.id
            val updatedEpisodes = episodes.map { if (it.seasonId != seasonId) it.copy(seasonId = seasonId) else it }
            episodeSyncer.sync(episodesDao.episodesWithSeasonId(seasonId), updatedEpisodes)
        }
    }

    suspend fun deleteShowSeasonData(showId: Long) {
        // Due to foreign keys, this will also delete the episodes and watches
        seasonsDao.deleteSeasonsForShowId(showId)
    }
}
