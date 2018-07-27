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
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import javax.inject.Inject
import javax.inject.Provider

class FollowedShowsRepository @Inject constructor(
    private val localStore: LocalFollowedShowsStore,
    private val localShowStore: LocalShowStore,
    private val traktDataSource: TraktFollowedShowsDataSource,
    private val showRepository: ShowRepository,
    private val traktAuthState: Provider<TraktAuthState>,
    private val logger: Logger
) {
    fun observeFollowedShows() = localStore.observeForPaging()

    suspend fun getFollowedShows(): List<FollowedShowEntry> {
        syncFollowedShows()
        return localStore.getEntries()
    }

    suspend fun syncFollowedShows() {
        val listId = getFollowedTraktListId()

        processPendingAdditions(listId)
        processPendingDelete(listId)

        if (listId != null) {
            pullDownTraktFollowedList(listId)
        }
    }

    private suspend fun pullDownTraktFollowedList(listId: Int) {
        traktDataSource.getListShows(listId)
                .map {
                    // Grab the show id if it exists, or save the show and use it's generated ID
                    val showId = localShowStore.getIdOrSavePlaceholder(it.show)
                    // Make a copy of the entry with the id
                    it.entry!!.copy(showId = showId)
                }
                .also {
                    // Save the related entries
                    localStore.sync(it)
                    // Now update all of the related shows if needed
                    it.parallelForEach { showRepository.updateShow(it.showId) }
                }
    }

    private suspend fun processPendingAdditions(listId: Int?) {
        val sendActions = localStore.getEntriesWithAddAction()

        if (sendActions.isNotEmpty()) {
            if (listId != null && traktAuthState.get() == TraktAuthState.LOGGED_IN) {
                val traktIds = sendActions.mapNotNull {
                    val show = showRepository.getShow(it.showId)
                    show.traktId?.toLong()
                }
                traktDataSource.addShowIdsToList(listId, traktIds)
            }

            // Now update the database
            localStore.updateEntriesWithAction(sendActions.mapNotNull { it.id }, PendingAction.NOTHING)
        }
    }

    private suspend fun processPendingDelete(listId: Int?) {
        val sendActions = localStore.getEntriesWithDeleteAction()

        if (sendActions.isNotEmpty()) {
            if (listId != null && traktAuthState.get() == TraktAuthState.LOGGED_IN) {
                val traktIds = sendActions.mapNotNull {
                    val show = showRepository.getShow(it.showId)
                    show.traktId?.toLong()
                }
                traktDataSource.removeShowIdsFromList(listId, traktIds)
            }

            // Now update the database
            localStore.deleteEntriesInIds(sendActions.mapNotNull { it.id })
        }
    }

    private suspend fun getFollowedTraktListId(): Int? {
        return localStore.traktListId
                ?: traktDataSource.getFollowedListId().also { localStore.traktListId = it }
    }
}