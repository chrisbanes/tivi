/*
 * Copyright 2018 Google LLC
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
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Request
import app.tivi.data.entities.Season
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.data.syncers.syncerForEntity
import io.reactivex.Flowable
import org.threeten.bp.temporal.TemporalAmount
import javax.inject.Inject

class LocalSeasonsEpisodesStore @Inject constructor(
    private val entityInserter: EntityInserter,
    private val transactionRunner: DatabaseTransactionRunner,
    private val seasonsDao: SeasonsDao,
    private val episodesDao: EpisodesDao,
    private val episodeWatchEntryDao: EpisodeWatchEntryDao,
    private val lastRequestDao: LastRequestDao
) {
    private val seasonSyncer = syncerForEntity(
            seasonsDao,
            { it.traktId },
            { entity, id -> entity.copy(id = id ?: 0) }
    )

    private val episodeSyncer = syncerForEntity(
            episodesDao,
            { it.traktId },
            { entity, id -> entity.copy(id = id ?: 0) }
    )

    private val episodeWatchSyncer = syncerForEntity(
            episodeWatchEntryDao,
            { it.traktId },
            { entity, id -> entity.copy(id = id ?: 0) }
    )

    fun observeEpisode(episodeId: Long): Flowable<Episode> {
        return episodesDao.episodeWithIdFlowable(episodeId)
    }

    fun observeEpisodeWatches(episodeId: Long): Flowable<List<EpisodeWatchEntry>> {
        return episodeWatchEntryDao.watchesForEpisodeFlowable(episodeId)
    }

    fun observeShowSeasonsWithEpisodes(showId: Long): Flowable<List<SeasonWithEpisodesAndWatches>> {
        return seasonsDao.seasonsWithEpisodesForShowId(showId)
    }

    /**
     * Gets the ID for the season with the given trakt Id. If the trakt Id does not exist in the
     * database, it is inserted and the generated ID is returned.
     */
    fun getEpisodeIdOrSavePlaceholder(episode: Episode): Long = transactionRunner {
        val episodeWithTraktId = episode.traktId?.let { episodesDao.episodeIdWithTraktId(it) }
        episodeWithTraktId ?: episodesDao.insert(episode)
    }

    /**
     * Gets the ID for the season with the given trakt Id. If the trakt Id does not exist in the
     * database, it is inserted and the generated ID is returned.
     */
    fun getEpisodeIdForTraktId(traktId: Int): Long? {
        return episodesDao.episodeIdWithTraktId(traktId)
    }

    fun getSeason(id: Long) = seasonsDao.seasonWithId(id)

    fun getSeasonWithTraktId(traktId: Int) = seasonsDao.seasonWithTraktId(traktId)

    fun getEpisodesInSeason(seasonId: Long) = episodesDao.episodesWithSeasonId(seasonId)

    fun getEpisode(id: Long) = episodesDao.episodeWithId(id)

    fun getEpisodeWithTraktId(traktId: Int) = episodesDao.episodeWithTraktId(traktId)

    fun save(episode: Episode) = entityInserter.insertOrUpdate(episodesDao, episode)

    fun save(watch: EpisodeWatchEntry) = entityInserter.insertOrUpdate(episodeWatchEntryDao, watch)

    fun saveWatches(watches: List<EpisodeWatchEntry>) = entityInserter.insertOrUpdate(episodeWatchEntryDao, watches)

    fun save(showId: Long, data: Map<Season, List<Episode>>) = transactionRunner {
        seasonSyncer.sync(seasonsDao.seasonsForShowId(showId), data.keys)
        data.forEach { (season, episodes) ->
            val seasonId = seasonsDao.seasonWithTraktId(season.traktId!!)!!.id
            val updatedEpisodes = episodes.map { if (it.seasonId != seasonId) it.copy(seasonId = seasonId) else it }
            episodeSyncer.sync(episodesDao.episodesWithSeasonId(seasonId), updatedEpisodes)
        }
    }

    fun lastShowSeasonsFetchBefore(showId: Long, threshold: TemporalAmount): Boolean {
        return lastRequestDao.isRequestBefore(Request.SHOW_SEASONS, showId, threshold)
    }

    fun updateShowSeasonsFetchLastRequest(showId: Long) {
        lastRequestDao.updateLastRequest(Request.SHOW_SEASONS, showId)
    }

    fun lastShowEpisodeWatchesSyncBefore(showId: Long, threshold: TemporalAmount): Boolean {
        return lastRequestDao.isRequestBefore(Request.SHOW_EPISODE_WATCHES, showId, threshold)
    }

    fun updateShowEpisodeWatchesLastRequest(showId: Long) {
        lastRequestDao.updateLastRequest(Request.SHOW_EPISODE_WATCHES, showId)
    }

    fun getEpisodeWatchesForShow(showId: Long) = episodeWatchEntryDao.entriesForShowId(showId)

    fun getWatchesForEpisode(episodeId: Long) = episodeWatchEntryDao.watchesForEpisode(episodeId)

    fun getEpisodeWatch(watchId: Long) = episodeWatchEntryDao.entryWithId(watchId)

    fun hasEpisodeBeenWatched(episodeId: Long) = episodeWatchEntryDao.watchCountForEpisode(episodeId) > 0

    fun getEntriesWithAddAction(showId: Long) = episodeWatchEntryDao.entriesForShowIdWithSendPendingActions(showId)

    fun getEntriesWithDeleteAction(showId: Long) = episodeWatchEntryDao.entriesForShowIdWithDeletePendingActions(showId)

    fun deleteWatchEntriesWithIds(ids: List<Long>) = episodeWatchEntryDao.deleteWithIds(ids)

    fun deleteShowSeasonData(showId: Long) {
        // Due to foreign keys, this will also delete the episodes and watches
        seasonsDao.deleteSeasonsForShowId(showId)
    }

    fun updateWatchEntriesWithAction(ids: List<Long>, action: PendingAction): Int {
        return episodeWatchEntryDao.updateEntriesToPendingAction(ids, action.value)
    }

    fun syncShowWatchEntries(showId: Long, watches: List<EpisodeWatchEntry>) = transactionRunner {
        val currentWatches = episodeWatchEntryDao.entriesForShowIdWithNoPendingAction(showId)
        episodeWatchSyncer.sync(currentWatches, watches)
    }

    fun syncEpisodeWatchEntries(episodeId: Long, watches: List<EpisodeWatchEntry>) = transactionRunner {
        val currentWatches = episodeWatchEntryDao.watchesForEpisode(episodeId)
        episodeWatchSyncer.sync(currentWatches, watches)
    }
}