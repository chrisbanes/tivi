// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes

import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.PendingAction
import app.tivi.data.util.syncerForEntity
import app.tivi.util.Logger
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class EpisodeWatchStore(
    private val transactionRunner: DatabaseTransactionRunner,
    private val episodeWatchEntryDao: EpisodeWatchEntryDao,
    logger: Logger,
) {
    private val episodeWatchSyncer = syncerForEntity(
        entityDao = episodeWatchEntryDao,
        entityToKey = { it.traktId },
        mapper = { newEntity, currentEntity -> newEntity.copy(id = currentEntity?.id ?: 0) },
        logger = logger,
    )

    fun observeEpisodeWatches(episodeId: Long): Flow<List<EpisodeWatchEntry>> {
        return episodeWatchEntryDao.watchesForEpisodeObservable(episodeId)
    }

    fun save(watch: EpisodeWatchEntry): Long = episodeWatchEntryDao.upsert(watch)

    fun save(watches: List<EpisodeWatchEntry>): Unit = episodeWatchEntryDao.upsert(watches)

    fun getEpisodeWatchesForShow(showId: Long) = episodeWatchEntryDao.entriesForShowId(showId)

    fun getWatchesForEpisode(episodeId: Long) = episodeWatchEntryDao.watchesForEpisode(episodeId)

    fun getEpisodeWatch(watchId: Long) = episodeWatchEntryDao.entryWithId(watchId)

    fun hasEpisodeBeenWatched(episodeId: Long) = episodeWatchEntryDao.watchCountForEpisode(episodeId) > 0

    fun getEntriesWithAddAction(showId: Long) = episodeWatchEntryDao.entriesForShowIdWithSendPendingActions(showId)

    fun getEntriesWithDeleteAction(showId: Long) = episodeWatchEntryDao.entriesForShowIdWithDeletePendingActions(showId)

    fun deleteEntriesWithIds(ids: List<Long>) = transactionRunner {
        episodeWatchEntryDao.deleteWithIds(ids)
    }

    fun updateEntriesWithAction(ids: List<Long>, action: PendingAction) = transactionRunner {
        episodeWatchEntryDao.updateEntriesToPendingAction(ids, action)
    }

    fun syncShowWatchEntries(
        showId: Long,
        watches: List<EpisodeWatchEntry>,
    ) = transactionRunner {
        val currentWatches = episodeWatchEntryDao.entriesForShowIdWithNoPendingAction(showId)
        episodeWatchSyncer.sync(currentWatches, watches)
    }

    fun syncEpisodeWatchEntries(
        episodeId: Long,
        watches: List<EpisodeWatchEntry>,
    ) = transactionRunner {
        val currentWatches = episodeWatchEntryDao.watchesForEpisode(episodeId)
        episodeWatchSyncer.sync(currentWatches, watches)
    }
}
