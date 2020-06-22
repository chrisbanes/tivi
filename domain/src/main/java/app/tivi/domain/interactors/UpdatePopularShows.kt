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

package app.tivi.domain.interactors

import app.tivi.data.daos.PopularDao
import app.tivi.data.fetch
import app.tivi.data.fetchCollection
import app.tivi.data.repositories.popularshows.PopularShowsLastRequestStore
import app.tivi.data.repositories.popularshows.PopularShowsStore
import app.tivi.data.repositories.showimages.ShowImagesStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdatePopularShows.Params
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.threeten.bp.Period
import javax.inject.Inject

class UpdatePopularShows @Inject constructor(
    private val popularShowStore: PopularShowsStore,
    private val popularDao: PopularDao,
    private val lastRequestStore: PopularShowsLastRequestStore,
    private val showsStore: ShowStore,
    private val showImagesStore: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers
) : Interactor<Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            val lastPage = popularDao.getLastPage()
            val page = when {
                lastPage != null && params.page == Page.NEXT_PAGE -> lastPage + 1
                else -> 0
            }

            popularShowStore.fetchCollection(page, forceFresh = params.forceRefresh) {
                // Refresh if our local data is over 7 days old
                page == 0 && lastRequestStore.isRequestExpired(Period.ofDays(7))
            }.asFlow().collect {
                showsStore.fetch(it.showId)
                showImagesStore.fetchCollection(it.showId)
            }
        }
    }

    data class Params(val page: Page, val forceRefresh: Boolean)

    enum class Page {
        NEXT_PAGE, REFRESH
    }
}
