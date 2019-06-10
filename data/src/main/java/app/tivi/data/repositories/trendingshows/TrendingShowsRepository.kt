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

package app.tivi.data.repositories.trendingshows

import app.tivi.data.entities.Success
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.extensions.parallelForEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendingShowsRepository @Inject constructor(
    private val trendingShowsStore: TrendingShowsStore,
    private val showStore: ShowStore,
    private val traktDataSource: TraktTrendingShowsDataSource,
    private val showRepository: ShowRepository
) {
    fun observeForPaging() = trendingShowsStore.observeForPaging()

    fun observeForObservable() = trendingShowsStore.observeForObservable(15, 0)

    suspend fun loadNextPage() {
        val lastPage = trendingShowsStore.getLastPage()
        if (lastPage != null) updateTrendingShows(lastPage + 1, false) else refresh()
    }

    suspend fun refresh() {
        updateTrendingShows(0, true)
    }

    private suspend fun updateTrendingShows(page: Int, resetOnSave: Boolean) {
        val response = traktDataSource.getTrendingShows(page, 20)
        when {
            response is Success && response.responseModified -> {
                response.data.map { (show, entry) ->
                    // Grab the show id if it exists, or save the show and use it's generated ID
                    val showId = showStore.getIdOrSavePlaceholder(show)
                    // Make a copy of the entry with the id
                    entry.copy(showId = showId, page = page)
                }.also { entries ->
                    if (resetOnSave) {
                        trendingShowsStore.deleteAll()
                    }
                    // Save the related entriesWithShows
                    trendingShowsStore.saveTrendingShowsPage(page, entries)
                    // Now update all of the related shows if needed
                    entries.parallelForEach { entry ->
                        if (showRepository.needsInitialUpdate(entry.showId)) {
                            showRepository.updateShow(entry.showId)
                        }
                        if (showRepository.needsImagesUpdate(entry.showId)) {
                            showRepository.updateShowImages(entry.showId)
                        }
                    }
                }
            }
        }
    }
}