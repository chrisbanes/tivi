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
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.Success
import app.tivi.data.fetch
import app.tivi.data.fetchCollection
import app.tivi.data.instantInPast
import app.tivi.data.repositories.shows.ShowImagesStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.extensions.asyncOrAwait
import app.tivi.extensions.parallelForEach
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.Instant

@Singleton
class PopularShowsRepository @Inject constructor(
    private val popularShowsStore: PopularShowsStore,
    private val lastRequestStore: PopularShowsLastRequestStore,
    private val traktDataSource: TraktPopularShowsDataSource,
    private val showDao: TiviShowDao,
    private val showStore: ShowStore,
    private val showImagesStore: ShowImagesStore
) {
    fun observeForPaging(): DataSource.Factory<Int, PopularEntryWithShow> = popularShowsStore.observeForPaging()

    fun observeForObservable(): Flow<List<PopularEntryWithShow>> {
        return popularShowsStore.observeForObservable(15, 0)
    }

    suspend fun loadNextPage() {
        val lastPage = popularShowsStore.getLastPage()
        if (lastPage != null) updatePopularShows(lastPage + 1, false) else update()
    }

    suspend fun needUpdate(expiry: Instant = instantInPast(days = 7)): Boolean {
        return lastRequestStore.isRequestBefore(expiry)
    }

    suspend fun update() {
        updatePopularShows(0, true)
        lastRequestStore.updateLastRequest()
    }

    private suspend fun updatePopularShows(page: Int, resetOnSave: Boolean) {
        asyncOrAwait("update_popular_page$page") {
            val response = traktDataSource.getPopularShows(page, 20)
            if (response is Success) {
                response.data.map { (show, entry) ->
                    // Grab the show id if it exists, or save the show and use it's generated ID
                    val showId = showDao.getIdOrSavePlaceholder(show)
                    // Make a copy of the entry with the id
                    entry.copy(showId = showId, page = page)
                }.also { entries ->
                    if (resetOnSave) {
                        popularShowsStore.deleteAll()
                    }
                    // Save the popular entriesWithShows
                    popularShowsStore.savePopularShowsPage(page, entries)
                    // Now update all of the related shows if needed
                    entries.parallelForEach { entry ->
                        showStore.fetch(entry.showId)
                        showImagesStore.fetchCollection(entry.showId)
                    }
                }
            }
        }
    }
}
