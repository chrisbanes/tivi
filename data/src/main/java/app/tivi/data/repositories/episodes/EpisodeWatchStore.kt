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
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.syncers.syncerForEntity
import app.tivi.util.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EpisodeWatchStore @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val episodeWatchEntryDao: EpisodeWatchEntryDao,
    private val logger: Logger
) {
    private val episodeWatchSyncer = syncerForEntity(
        episodeWatchEntryDao,
        { it.traktId },
        { entity, id -> entity.copy(id = id ?: 0) },
        logger
    )

    fun observeEpisodeWatches(episodeId: Long): Flow<List<EpisodeWatchEntry>> {
        return episodeWatchEntryDao.watchesForEpisodeObservable(episodeId)
    }

    suspend fun save(watch: EpisodeWatchEntry) = episodeWatchEntryDao.insertOrUpdate(watch)

    suspend fun save(watches: List<EpisodeWatchEntry>) = episodeWatchEntryDao.insertOrUpdate(watches)

    suspend fun getEpisodeWatchesForShow(showId: Long) = episodeWatchEntryDao.entriesForShowId(showId)

    suspend fun getWatchesForEpisode(episodeId: Long) = episodeWatchEntryDao.watchesForEpisode(episodeId)

    suspend fun getEpisodeWatch(watchId: Long) = episodeWatchEntryDao.entryWithId(watchId)

    suspend fun hasEpisodeBeenWatched(episodeId: Long) = episodeWatchEntryDao.watchCountForEpisode(episodeId) > 0

    suspend fun getEntriesWithAddAction(showId: Long) = episodeWatchEntryDao.entriesForShowIdWithSendPendingActions(showId)

    suspend fun getEntriesWithDeleteAction(showId: Long) = episodeWatchEntryDao.entriesForShowIdWithDeletePendingActions(showId)

    suspend fun deleteEntriesWithIds(ids: List<Long>) = episodeWatchEntryDao.deleteWithIds(ids)

    suspend fun updateEntriesWithAction(ids: List<Long>, action: PendingAction): Int {
        return episodeWatchEntryDao.updateEntriesToPendingAction(ids, action.value)
    }

    suspend fun addNewShowWatchEntries(
        showId: Long,
        watches: List<EpisodeWatchEntry>
    ) = transactionRunner {
        val currentWatches = episodeWatchEntryDao.entriesForShowIdWithNoPendingAction(showId)
        episodeWatchSyncer.sync(currentWatches, watches, removeNotMatched = false)
    }

    suspend fun syncShowWatchEntries(
        showId: Long,
        watches: List<EpisodeWatchEntry>
    ) = transactionRunner {
        val currentWatches = episodeWatchEntryDao.entriesForShowIdWithNoPendingAction(showId)
        episodeWatchSyncer.sync(currentWatches, watches)
    }

    suspend fun syncEpisodeWatchEntries(
        episodeId: Long,
        watches: List<EpisodeWatchEntry>
    ) = transactionRunner {
        val currentWatches = episodeWatchEntryDao.watchesForEpisode(episodeId)
        episodeWatchSyncer.sync(currentWatches, watches)
    }
}
