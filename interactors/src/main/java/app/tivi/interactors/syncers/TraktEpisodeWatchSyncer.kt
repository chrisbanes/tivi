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

package app.tivi.interactors.syncers

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Request.SHOW_EPISODE_WATCHES
import app.tivi.extensions.fetchBody
import app.tivi.extensions.fetchBodyWithRetry
import app.tivi.trakt.TraktAuthState
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
import org.threeten.bp.Period
import javax.inject.Inject
import javax.inject.Provider

class TraktEpisodeWatchSyncer @Inject constructor(
    private val lastRequests: LastRequestDao,
    private val episodeWatchEntryDao: EpisodeWatchEntryDao,
    private val showDao: TiviShowDao,
    private val episodesDao: EpisodesDao,
    private val usersService: Provider<Users>,
    private val syncService: Provider<Sync>,
    private val databaseTransactionRunner: DatabaseTransactionRunner,
    private val logger: Logger,
    private val traktAuthState: Provider<TraktAuthState>
) {
    private val watchSyncer = syncerForEntity(
            episodeWatchEntryDao,
            EpisodeWatchEntry::traktId,
            HistoryEntry::id,
            ::mapToEpisodeWatchEntry,
            logger
    )

    fun pushPendingToTrakt(showId: Long) = processPendingDeleteWatches(showId) || processPendingSendWatches(showId)

    suspend fun sync(showId: Long, forceSync: Boolean = false) {
        var sync = forceSync
        sync = processPendingDeleteWatches(showId) || sync
        sync = processPendingSendWatches(showId) || sync

        if (sync || shouldUpdate(showId)) {
            val show = showDao.getShowWithId(showId)!!
            refreshWatchesFromTrakt(showId, show.traktId!!)
            // Now update timestamp
            lastRequests.updateLastRequest(SHOW_EPISODE_WATCHES, showId)
        }
    }

    private fun shouldUpdate(showId: Long): Boolean {
        return lastRequests.isRequestBefore(SHOW_EPISODE_WATCHES, showId, Period.ofDays(1))
    }

    /**
     * Send any pending watches to Trakt.
     *
     * @return true if there were any pending actions
     */
    fun processPendingSendWatches(showId: Long): Boolean {
        val sendActions = episodeWatchEntryDao.entriesForShowIdWithSendPendingActions(showId)
                .map { it to episodesDao.episodeWithId(it.episodeId)!! }

        if (sendActions.isNotEmpty()) {
            val items = SyncItems()
            items.episodes = sendActions.map { (entry, episode) ->
                SyncEpisode().apply {
                    watched_at = entry.watchedAt
                    ids = EpisodeIds.trakt(episode.traktId!!)
                }
            }

            if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
                val response = syncService.get().addItemsToWatchedHistory(items).fetchBody()

                if (response.added.episodes != sendActions.size) {
                    // TODO Something has gone wrong here, lets check the not found list
                }
            }

            // Now update the database
            episodeWatchEntryDao.updateEntriesToPendingAction(
                    sendActions.mapNotNull { it.first.id },
                    PendingAction.NOTHING.value
            )
            return true
        }

        return false
    }

    /**
     * Send any pending deletes to Trakt.
     *
     * @return true if there were any pending actions
     */
    fun processPendingDeleteWatches(showId: Long): Boolean {
        val deleteActions = episodeWatchEntryDao.entriesForShowIdWithDeletePendingActions(showId)

        if (deleteActions.isNotEmpty()) {
            val deleteIds = deleteActions.mapNotNull { it.traktId }

            if (deleteIds.isNotEmpty()) {
                logger.d("Deleting watches from Trakt: $deleteIds")

                if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
                    val items = SyncItems()
                    items.ids = deleteIds

                    val response = syncService.get().deleteItemsFromWatchedHistory(items).fetchBody()

                    logger.d("Response from deleting watches from Trakt: $response")

                    if (response.deleted.episodes != deleteActions.size) {
                        // TODO Something has gone wrong here, lets check the not found list
                    }
                }
            }
            // Now update the database
            episodeWatchEntryDao.deleteWithIds(deleteActions.mapNotNull { it.id })

            return true
        }

        return false
    }

    private suspend fun refreshWatchesFromTrakt(showId: Long, traktId: Int) {
        if (traktAuthState.get() != TraktAuthState.LOGGED_IN) {
            logger.i("Not logged in. Can't refreshWatchesFromTrakt()")
            return
        }
        // Fetch watched progress for show and filter out watches
        val watchedProgress = usersService.get().history(UserSlug.ME, HistoryType.SHOWS, traktId,
                0, 10000, Extended.NOSEASONS, null, null)
                .fetchBodyWithRetry()
                .filter { it.type == "episode" }
        // and sync the result
        syncWatchesFromTrakt(showId, watchedProgress)
    }

    fun syncWatchesFromTrakt(showId: Long, watches: List<HistoryEntry>) {
        databaseTransactionRunner.runInTransaction {
            val currentWatches = episodeWatchEntryDao.entriesForShowIdWithNoPendingAction(showId)
            watchSyncer.sync(currentWatches, watches)
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
                watchedAt = historyEntry.watched_at
        )
    }
}