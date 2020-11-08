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

package app.tivi.data.repositories.followedshows

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.Success
import app.tivi.data.instantInPast
import app.tivi.data.syncers.ItemSyncerResult
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class FollowedShowsRepository @Inject constructor(
    private val followedShowsStore: FollowedShowsStore,
    private val followedShowsLastRequestStore: FollowedShowsLastRequestStore,
    private val dataSource: TraktFollowedShowsDataSource,
    private val traktAuthState: Provider<TraktAuthState>,
    private val logger: Logger,
    private val showDao: TiviShowDao
) {
    fun observeFollowedShows(
        sort: SortOption,
        filter: String? = null
    ) = followedShowsStore.observeForPaging(sort, filter)

    fun observeShowViewStats(showId: Long) = followedShowsStore.observeShowViewStats(showId)

    fun observeIsShowFollowed(showId: Long) = followedShowsStore.observeIsShowFollowed(showId)

    fun observeNextShowToWatch() = followedShowsStore.observeNextShowToWatch()

    suspend fun isShowFollowed(showId: Long) = followedShowsStore.isShowFollowed(showId)

    suspend fun getFollowedShows(): List<FollowedShowEntry> {
        return followedShowsStore.getEntries()
    }

    suspend fun needFollowedShowsSync(expiry: Instant = instantInPast(hours = 1)): Boolean {
        return followedShowsLastRequestStore.isRequestBefore(expiry)
    }

    suspend fun addFollowedShow(showId: Long) {
        val entry = followedShowsStore.getEntryForShowId(showId)

        logger.d("addFollowedShow. Current entry: %s", entry)

        if (entry == null || entry.pendingAction == PendingAction.DELETE) {
            // If we don't have an entry, or it is marked for deletion, lets update it to be uploaded
            val newEntry = FollowedShowEntry(
                id = entry?.id ?: 0,
                showId = showId,
                followedAt = entry?.followedAt ?: OffsetDateTime.now(),
                pendingAction = PendingAction.UPLOAD
            )
            val newEntryId = followedShowsStore.save(newEntry)

            logger.v("addFollowedShow. Entry saved with ID: %s - %s", newEntryId, newEntry)
        }
    }

    suspend fun removeFollowedShow(showId: Long) {
        // Update the followed show to be deleted
        val entry = followedShowsStore.getEntryForShowId(showId)
        if (entry != null) {
            // Mark the show as pending deletion
            followedShowsStore.save(entry.copy(pendingAction = PendingAction.DELETE))
        }
    }

    suspend fun syncFollowedShows(): ItemSyncerResult<FollowedShowEntry> {
        val listId = when (traktAuthState.get()) {
            TraktAuthState.LOGGED_IN -> getFollowedTraktListId()
            else -> null
        }

        processPendingAdditions(listId)
        processPendingDelete(listId)

        return when {
            listId != null -> pullDownTraktFollowedList(listId)
            else -> ItemSyncerResult()
        }.also {
            followedShowsLastRequestStore.updateLastRequest()
        }
    }

    private suspend fun pullDownTraktFollowedList(
        listId: Int
    ): ItemSyncerResult<FollowedShowEntry> {
        val response = dataSource.getListShows(listId)
        logger.d("pullDownTraktFollowedList. Response: %s", response)
        return response.getOrThrow().map { (entry, show) ->
            // Grab the show id if it exists, or save the show and use it's generated ID
            val showId = showDao.getIdOrSavePlaceholder(show)
            // Create a followed show entry with the show id
            entry.copy(showId = showId)
        }.let { entries ->
            // Save the show entries
            followedShowsStore.sync(entries)
        }
    }

    private suspend fun processPendingAdditions(listId: Int?) {
        val pending = followedShowsStore.getEntriesWithAddAction()
        logger.d("processPendingAdditions. listId: %s, Entries: %s", listId, pending)

        if (pending.isEmpty()) {
            return
        }

        if (listId != null && traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            val shows = pending.mapNotNull { showDao.getShowWithId(it.showId) }
            logger.v("processPendingAdditions. Entries mapped: %s", shows)

            val response = dataSource.addShowIdsToList(listId, shows)
            logger.v("processPendingAdditions. Trakt response: %s", response)

            if (response is Success) {
                // Now update the database
                followedShowsStore.updateEntriesWithAction(pending.map { it.id }, PendingAction.NOTHING)
            }
        } else {
            // We're not logged in, so just update the database
            followedShowsStore.updateEntriesWithAction(pending.map { it.id }, PendingAction.NOTHING)
        }
    }

    private suspend fun processPendingDelete(listId: Int?) {
        val pending = followedShowsStore.getEntriesWithDeleteAction()
        logger.d("processPendingDelete. listId: %s, Entries: %s", listId, pending)

        if (pending.isEmpty()) {
            return
        }

        if (listId != null && traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            val shows = pending.mapNotNull { showDao.getShowWithId(it.showId) }
            logger.v("processPendingDelete. Entries mapped: %s", shows)

            val response = dataSource.removeShowIdsFromList(listId, shows)
            logger.v("processPendingDelete. Trakt response: %s", response)

            if (response is Success) {
                // Now update the database
                followedShowsStore.deleteEntriesInIds(pending.map { it.id })
            }
        } else {
            // We're not logged in, so just update the database
            followedShowsStore.deleteEntriesInIds(pending.map { it.id })
        }
    }

    private suspend fun getFollowedTraktListId(): Int? {
        if (followedShowsStore.traktListId == null) {
            val result = dataSource.getFollowedListId()
            if (result is Success) {
                followedShowsStore.traktListId = result.get()
            }
        }
        return followedShowsStore.traktListId
    }
}
