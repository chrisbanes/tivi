/*
 * Copyright 2019 Google LLC
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

package app.tivi.data.repositories.recommendedshows

import app.tivi.data.entities.RecommendedShowEntry
import app.tivi.data.entities.Success
import app.tivi.data.instantInPast
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.extensions.parallelForEach
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendedShowsRepository @Inject constructor(
    private val recommendedShowsStore: RecommendedShowsStore,
    private val lastRequestStore: RecommendedShowsLastRequestStore,
    private val showStore: ShowStore,
    private val traktDataSource: TraktRecommendedShowsDataSource,
    private val showRepository: ShowRepository
) {
    fun observeForPaging() = recommendedShowsStore.observeForPaging()

    fun observeForObservable() = recommendedShowsStore.observeForObservable(15, 0)

    suspend fun loadNextPage() {
        val lastPage = recommendedShowsStore.getLastPage()
        if (lastPage != null) updateFromDataSource(lastPage + 1, false) else update()
    }

    suspend fun needUpdate(expiry: Instant = instantInPast(days = 3)): Boolean {
        return lastRequestStore.isRequestBefore(expiry)
    }

    suspend fun update() {
        updateFromDataSource(0, true)
        lastRequestStore.updateLastRequest()
    }

    private suspend fun updateFromDataSource(page: Int, resetOnSave: Boolean) {
        when (val response = traktDataSource.getRecommendedShows(page, 20)) {
            is Success -> {
                response.data.map { show ->
                    // Grab the show id if it exists, or save the show and use it's generated ID
                    val showId = showStore.getIdOrSavePlaceholder(show)
                    // Map to an entry
                    RecommendedShowEntry(showId = showId, page = page)
                }.also { entries ->
                    if (resetOnSave) {
                        recommendedShowsStore.deleteAll()
                    }
                    // Save the related entriesWithShows
                    recommendedShowsStore.savePage(page, entries)
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