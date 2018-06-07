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

package app.tivi.tasks

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.sync.syncerForEntity
import app.tivi.extensions.fetchBody
import app.tivi.extensions.fetchBodyWithRetry
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import com.uwetrottmann.trakt5.entities.EpisodeIds
import com.uwetrottmann.trakt5.entities.HistoryEntry
import com.uwetrottmann.trakt5.entities.SyncEpisode
import com.uwetrottmann.trakt5.entities.SyncItems
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.enums.HistoryType
import com.uwetrottmann.trakt5.services.Sync
import com.uwetrottmann.trakt5.services.Users
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject
import javax.inject.Provider

class TraktEpisodeWatchSyncer @Inject constructor(
    private val episodeWatchEntryDao: EpisodeWatchEntryDao,
    private val showDao: TiviShowDao,
    private val episodesDao: EpisodesDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val usersService: Provider<Users>,
    private val syncService: Provider<Sync>,
    private val databaseTransactionRunner: DatabaseTransactionRunner,
    private val logger: Logger
) {
    private val watchSyncer = syncerForEntity(
            episodeWatchEntryDao,
            EpisodeWatchEntry::traktId,
            HistoryEntry::id,
            ::mapToEpisodeWatchEntry,
            logger
    )

    suspend fun sync(showId: Long) {
        sendPendingDeleteWatchesToTrakt(showId)
        sendPendingSendWatchesToTrakt(showId)
        refreshWatchesFromTrakt(showId)
    }

    suspend fun sendPendingSendWatchesToTrakt(showId: Long) {
        val sendActions = withContext(dispatchers.database) {
            episodeWatchEntryDao.entriesForShowIdWithSendPendingActions(showId)
                    .map { it to episodesDao.episodeWithId(it.episodeId)!! }
        }

        if (sendActions.isNotEmpty()) {
            val items = SyncItems()
            items.episodes = sendActions.map { (entry, episode) ->
                SyncEpisode().apply {
                    watched_at = entry.watchedAt
                    ids = EpisodeIds.trakt(episode.traktId!!)
                }
            }

            val response = withContext(dispatchers.network) {
                syncService.get().addItemsToWatchedHistory(items).fetchBody()
            }

            if (response.added.episodes != sendActions.size) {
                // TODO Something has gone wrong here, lets check the not found list
            }

            // Now update the database
            withContext(dispatchers.database) {
                databaseTransactionRunner.runInTransaction {
                    sendActions.forEach {
                        val entry = it.first.copy(pendingAction = EpisodeWatchEntry.PENDING_ACTION_NOTHING)
                        episodeWatchEntryDao.update(entry)
                    }
                }
            }
        }
    }

    suspend fun sendPendingDeleteWatchesToTrakt(showId: Long) {
        val deleteActions = withContext(dispatchers.database) {
            episodeWatchEntryDao.entriesForShowIdWithDeletePendingActions(showId)
        }
        if (deleteActions.isNotEmpty()) {
            val deleteIds = deleteActions.mapNotNull { it.traktId }

            if (deleteIds.isNotEmpty()) {
                logger.d("Deleting watches from Trakt: $deleteIds")

                val items = SyncItems()
                items.ids = deleteIds

                val response = withContext(dispatchers.network) {
                    syncService.get().deleteItemsFromWatchedHistory(items).fetchBody()
                }

                logger.d("Response from deleting watches from Trakt: $response")

                if (response.deleted.episodes != deleteActions.size) {
                    // TODO Something has gone wrong here, lets check the not found list
                }
            }

            // Now update the database
            withContext(dispatchers.database) {
                databaseTransactionRunner.runInTransaction {
                    deleteActions.forEach {
                        logger.d("Deleting local watch entry: $it")
                        episodeWatchEntryDao.deleteWithId(it.id!!)
                    }
                }
            }
        }
    }

    suspend fun refreshWatchesFromTrakt(showId: Long) {
        val show = withContext(dispatchers.database) {
            showDao.getShowWithId(showId)!!
        }
        // Fetch watched progress for show and filter out watches
        val watchedProgress = withContext(dispatchers.network) {
            // TODO use start and end dates
            usersService.get().history(UserSlug.ME, HistoryType.SHOWS, show.traktId!!,
                    0, 10000, Extended.NOSEASONS, null, null).fetchBodyWithRetry()
        }.filter {
            it.action == "watch" && it.type == "episode"
        }

        // and sync the result
        syncWatchesFromTrakt(showId, watchedProgress)
    }

    suspend fun syncWatchesFromTrakt(showId: Long, watches: List<HistoryEntry>) {
        withContext(dispatchers.database) {
            databaseTransactionRunner.runInTransaction {
                val currentWatches = episodeWatchEntryDao.entriesForShowIdWithNoPendingAction(showId)
                watchSyncer.sync(currentWatches, watches)
            }
        }
    }

    private fun mapToEpisodeWatchEntry(historyEntry: HistoryEntry, dbId: Long?): EpisodeWatchEntry {
        val episodeTraktId = historyEntry.episode.ids.trakt
        val episode = episodesDao.episodeWithTraktId(episodeTraktId)
                ?: throw IllegalArgumentException("Episode with traktId[$episodeTraktId] does not exist.")

        return EpisodeWatchEntry(
                id = dbId,
                episodeId = episode.id!!,
                traktId = historyEntry.id,
                watchedAt = historyEntry.watched_at,
                pendingAction = EpisodeWatchEntry.PENDING_ACTION_NOTHING
        )
    }
}