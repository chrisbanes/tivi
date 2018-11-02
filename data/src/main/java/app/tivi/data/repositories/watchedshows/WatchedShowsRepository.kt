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

package app.tivi.data.repositories.watchedshows

import app.tivi.data.entities.Success
import app.tivi.data.repositories.shows.LocalShowStore
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.extensions.parallelForEach
import app.tivi.util.AppCoroutineDispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchedShowsRepository @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val localStore: LocalWatchedShowsStore,
    private val localShowStore: LocalShowStore,
    private val traktDataSource: TraktWatchedShowsDataSource,
    private val showRepository: ShowRepository
) {
    fun observeWatchedShowsPagedList() = localStore.observePagedList()

    suspend fun getWatchedShows() {
        updateWatchedShows()
        localStore.getWatchedShows()
    }

    suspend fun updateWatchedShows() {
        val response = traktDataSource.getWatchedShows()
        when (response) {
            is Success -> {
                response.data.map { (show, entry) ->
                    // Grab the show id if it exists, or save the show and use it's generated ID
                    val watchedShowId = localShowStore.getIdOrSavePlaceholder(show)
                    // Make a copy of the entry with the id
                    entry.copy(showId = watchedShowId)
                }.also { entries ->
                    // Save the related entries
                    localStore.saveWatchedShows(entries)
                    // Now update all of the related shows if needed
                    entries.parallelForEach { entry ->
                        if (showRepository.needsUpdate(entry.showId)) {
                            showRepository.updateShow(entry.showId)
                        }
                    }
                }
            }
        }
    }
}