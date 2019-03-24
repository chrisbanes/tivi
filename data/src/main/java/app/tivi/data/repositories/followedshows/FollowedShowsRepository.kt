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

import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Success
import app.tivi.data.repositories.shows.LocalShowStore
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.extensions.parallelForEach
import app.tivi.inject.Trakt
import app.tivi.trakt.TraktAuthState
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class FollowedShowsRepository @Inject constructor(
    private val localStore: LocalFollowedShowsStore,
    private val localShowStore: LocalShowStore,
    @Trakt private val dataSource: FollowedShowsDataSource,
    private val showRepository: ShowRepository,
    private val traktAuthState: Provider<TraktAuthState>
) {
    fun observeFollowedShows() = localStore.observeForPaging()

    fun observeIsShowFollowed(showId: Long) = localStore.observeIsShowFollowed(showId)

    suspend fun isShowFollowed(showId: Long) = localStore.isShowFollowed(showId)

    suspend fun getFollowedShows(): List<FollowedShowEntry> {
        syncFollowedShows()
        return localStore.getEntries()
    }

    suspend fun needFollowedShowsSync(): Boolean {
        return localStore.isLastFollowedShowsSyncBefore(Duration.ofHours(3))
    }

    suspend fun toggleFollowedShow(showId: Long) {
        if (isShowFollowed(showId)) {
            removeFollowedShow(showId)
        } else {
            addFollowedShow(showId)
        }
    }

    suspend fun addFollowedShow(showId: Long) {
        val entry = localStore.getEntryForShowId(showId)
        if (entry == null || entry.pendingAction == PendingAction.DELETE) {
            // If we don't have an entry, or it is marked for deletion, lets update it to be uploaded
            val newEntry = FollowedShowEntry(
                    id = entry?.id ?: 0,
                    showId = showId,
                    followedAt = entry?.followedAt ?: OffsetDateTime.now(),
                    pendingAction = PendingAction.UPLOAD
            )
            localStore.save(newEntry)
            // Now sync it up
            syncFollowedShows()
        }
    }

    suspend fun removeFollowedShow(showId: Long) {
        // Update the followed show to be deleted
        val entry = localStore.getEntryForShowId(showId)
        if (entry != null) {
            // Mark the show as pending deletion
            localStore.save(entry.copy(pendingAction = PendingAction.DELETE))
            // Now sync it up
            syncFollowedShows()
        }
    }

    suspend fun syncFollowedShows() {
        val listId = if (traktAuthState.get() == TraktAuthState.LOGGED_IN) getFollowedTraktListId() else null

        processPendingAdditions(listId)
        processPendingDelete(listId)

        if (listId != null) {
            pullDownTraktFollowedList(listId)
        }

        localStore.updateLastFollowedShowsSync()
    }

    private suspend fun pullDownTraktFollowedList(listId: Int) {
        val response = dataSource.getListShows(listId)
        when (response) {
            is Success ->
                response.data.map { (entry, show) ->
                    // Grab the show id if it exists, or save the show and use it's generated ID
                    val showId = localShowStore.getIdOrSavePlaceholder(show)
                    // Create a followed show entry with the show id
                    entry.copy(showId = showId)
                }.also { entries ->
                    // Save the related entries
                    localStore.sync(entries)
                    // Now update all of the followed shows if needed
                    entries.parallelForEach { entry ->
                        if (showRepository.needsUpdate(entry.showId)) {
                            showRepository.updateShow(entry.showId)
                        }
                    }
                }
        }
    }

    private suspend fun processPendingAdditions(listId: Int?) {
        val pending = localStore.getEntriesWithAddAction()
        if (pending.isEmpty()) {
            return
        }

        if (listId != null && traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            val shows = pending.mapNotNull { localShowStore.getShow(it.showId) }
            val response = dataSource.addShowIdsToList(listId, shows)
            if (response is Success) {
                // Now update the database
                localStore.updateEntriesWithAction(pending.map { it.id }, PendingAction.NOTHING)
            }
        } else {
            // We're not logged in, so just update the database
            localStore.updateEntriesWithAction(pending.map { it.id }, PendingAction.NOTHING)
        }
    }

    private suspend fun processPendingDelete(listId: Int?) {
        val pending = localStore.getEntriesWithDeleteAction()
        if (pending.isEmpty()) {
            return
        }

        if (listId != null && traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            val shows = pending.mapNotNull { localShowStore.getShow(it.showId) }
            val response = dataSource.removeShowIdsFromList(listId, shows)
            if (response is Success) {
                // Now update the database
                localStore.deleteEntriesInIds(pending.map { it.id })
            }
        } else {
            // We're not logged in, so just update the database
            localStore.deleteEntriesInIds(pending.map { it.id })
        }
    }

    private suspend fun getFollowedTraktListId(): Int? {
        return localStore.traktListId ?: dataSource.getFollowedListId()?.also { localStore.traktListId = it }
    }
}