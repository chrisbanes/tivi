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

import app.tivi.ShowFetcher
import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.sync.syncerForEntity
import app.tivi.extensions.fetchBody
import app.tivi.extensions.fetchBodyWithRetry
import app.tivi.extensions.parallelForEach
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import com.uwetrottmann.trakt5.entities.Show
import com.uwetrottmann.trakt5.entities.ShowIds
import com.uwetrottmann.trakt5.entities.SyncItems
import com.uwetrottmann.trakt5.entities.SyncShow
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Users
import javax.inject.Inject
import javax.inject.Provider

open class TraktFollowedShowsSyncer @Inject constructor(
    private val dao: FollowedShowsDao,
    private val showDao: TiviShowDao,
    private val usersService: Provider<Users>,
    private val databaseTransactionRunner: DatabaseTransactionRunner,
    private val logger: Logger,
    private val traktAuthState: Provider<TraktAuthState>,
    private val showFetcher: ShowFetcher
) {
    private val syncer = syncerForEntity<FollowedShowEntry, Show, Int>(
            dao,
            { showDao.getTraktIdForShowId(it.showId)!! },
            { it.ids.trakt },
            ::mapToEpisodeWatchEntry,
            logger
    )

    suspend fun sync() {
        val listId = usersService.get().lists(UserSlug.ME).fetchBody()
                .first { it.name == "Following" }
                .ids
                .trakt

        processPendingDelete(listId)
        processPendingSend(listId)
        refreshFromTrakt(listId)
    }

    suspend fun processPendingSend(listId: Int) {
        val sendActions = dao.entriesWithSendPendingActions()

        if (sendActions.isNotEmpty()) {
            val items = SyncItems()
            items.shows = sendActions.map(this::mapToSyncShow)

            if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
                val response = usersService.get().addListItems(UserSlug.ME, listId.toString(), items)
                        .fetchBody()

                // TODO check response
            }

            // Now update the database
            dao.updateEntriesToPendingAction(
                    sendActions.mapNotNull(FollowedShowEntry::id),
                    PendingAction.NOTHING.value
            )
        }
    }

    fun processPendingDelete(listId: Int) {
        val deleteActions = dao.entriesWithDeletePendingActions()

        if (deleteActions.isNotEmpty()) {
            logger.d("Deleting followed show from Trakt: $deleteActions")

            if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
                val items = SyncItems()
                items.shows = deleteActions.map(this::mapToSyncShow)

                val response = usersService.get().deleteListItems(UserSlug.ME, listId.toString(), items).fetchBody()

                logger.d("Response from deleting items from Trakt: $response")

                if (response.deleted.episodes != deleteActions.size) {
                    // TODO Something has gone wrong here, lets check the not found list
                }
            }

            // Now update the database
            dao.deleteWithIds(deleteActions.mapNotNull(FollowedShowEntry::id))
        }
    }

    suspend fun refreshFromTrakt(listId: Int) {
        if (traktAuthState.get() != TraktAuthState.LOGGED_IN) {
            logger.i("Not logged in. Can't refreshFromTrakt()")
            return
        }

        // Fetch watched progress for show and filter out watches
        val traktResponse = usersService.get().listItems(UserSlug.ME, listId.toString(), Extended.NOSEASONS)
                .fetchBodyWithRetry()
                .mapNotNull { it.show }

        // and sync the result
        syncFromTraktResponse(traktResponse)
    }

    suspend fun syncFromTraktResponse(shows: List<Show>) {
        // Insert placeholders for shows
        shows.parallelForEach {
            showFetcher.insertPlaceholderIfNeeded(it)
        }

        // Now sync the entries
        databaseTransactionRunner.runInTransaction {
            syncer.sync(dao.entriesBlocking(), shows)
        }
    }

    private fun mapToEpisodeWatchEntry(show: Show, id: Long?): FollowedShowEntry {
        return FollowedShowEntry(
                id = id,
                showId = showDao.getIdForTraktId(show.ids.trakt)
        )
    }

    private fun mapToSyncShow(entry: FollowedShowEntry) = SyncShow().apply {
        ids = ShowIds.trakt(showDao.getTraktIdForShowId(entry.showId)!!)
    }
}