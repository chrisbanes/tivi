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

package app.tivi.data.repositories.followedshows

import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.repositories.shows.LocalShowStore
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.extensions.parallelForEach
import app.tivi.inject.Trakt
import app.tivi.trakt.TraktAuthState
import app.tivi.util.AppCoroutineDispatchers
import org.threeten.bp.Duration
import javax.inject.Inject
import javax.inject.Provider

class FollowedShowsRepository @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val localStore: LocalFollowedShowsStore,
    private val localShowStore: LocalShowStore,
    @Trakt private val dataSource: FollowedShowsDataSource,
    private val showRepository: ShowRepository,
    private val traktAuthState: Provider<TraktAuthState>
) {
    fun observeFollowedShows() = localStore.observeForPaging()

    suspend fun getFollowedShows(): List<FollowedShowEntry> {
        syncFollowedShows()
        return localStore.getEntries()
    }

    fun needFollowedShowsSync(): Boolean {
        return localStore.isLastFollowedShowsSyncBefore(Duration.ofHours(3))
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
        dataSource.getListShows(listId)
                .map {
                    // Grab the show id if it exists, or save the show and use it's generated ID
                    val showId = localShowStore.getIdOrSavePlaceholder(it)
                    // Create a followed show entry with the show id
                    FollowedShowEntry(showId = showId)
                }
                .also { entries ->
                    // Save the related entries
                    localStore.sync(entries)
                    // Now update all of the followed shows if needed
                    entries.parallelForEach(dispatchers.io) { entry ->
                        if (showRepository.needsUpdate(entry.showId)) {
                            showRepository.updateShow(entry.showId)
                        }
                    }
                }
    }

    private suspend fun processPendingAdditions(listId: Int?) {
        val pending = localStore.getEntriesWithAddAction()

        if (pending.isNotEmpty()) {
            if (listId != null && traktAuthState.get() == TraktAuthState.LOGGED_IN) {
                val shows = pending.mapNotNull { localShowStore.getShow(it.showId) }
                dataSource.addShowIdsToList(listId, shows)
            }

            // Now update the database
            localStore.updateEntriesWithAction(pending.mapNotNull { it.id }, PendingAction.NOTHING)
        }
    }

    private suspend fun processPendingDelete(listId: Int?) {
        val pending = localStore.getEntriesWithDeleteAction()

        if (pending.isNotEmpty()) {
            if (listId != null && traktAuthState.get() == TraktAuthState.LOGGED_IN) {
                val shows = pending.mapNotNull { localShowStore.getShow(it.showId) }
                dataSource.removeShowIdsFromList(listId, shows)
            }

            // Now update the database
            localStore.deleteEntriesInIds(pending.mapNotNull { it.id })
        }
    }

    private suspend fun getFollowedTraktListId(): Int? {
        return localStore.traktListId ?: dataSource.getFollowedListId()?.also { localStore.traktListId = it }
    }
}