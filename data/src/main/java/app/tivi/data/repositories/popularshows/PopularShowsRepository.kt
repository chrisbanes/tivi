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

package app.tivi.data.repositories.popularshows

import androidx.paging.DataSource
import app.tivi.data.entities.Success
import app.tivi.data.repositories.shows.LocalShowStore
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.extensions.parallelForEach
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PopularShowsRepository @Inject constructor(
    private val localStore: LocalPopularShowsStore,
    private val showStore: LocalShowStore,
    private val traktDataSource: TraktPopularShowsDataSource,
    private val showRepository: ShowRepository
) {
    fun observeForPaging(): DataSource.Factory<Int, PopularEntryWithShow> = localStore.observeForPaging()

    fun observeForFlowable(): Flowable<List<PopularEntryWithShow>> = localStore.observeForFlowable(10, 0)

    suspend fun loadNextPage() {
        val lastPage = localStore.getLastPage()
        if (lastPage != null) updatePopularShows(lastPage + 1, false) else refresh()
    }

    suspend fun refresh() {
        updatePopularShows(0, true)
    }

    private suspend fun updatePopularShows(page: Int, resetOnSave: Boolean) {
        val response = traktDataSource.getPopularShows(page, 20)
        when (response) {
            is Success -> {
                response.data.map { (show, entry) ->
                    // Grab the show id if it exists, or save the show and use it's generated ID
                    val showId = showStore.getIdOrSavePlaceholder(show)
                    // Make a copy of the entry with the id
                    entry.copy(showId = showId, page = page)
                }.also { entries ->
                    if (resetOnSave) {
                        localStore.deleteAll()
                    }
                    // Save the popular entries
                    localStore.savePopularShowsPage(page, entries)
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