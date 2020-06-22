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

import app.tivi.data.fetch
import app.tivi.data.fetchCollection
import app.tivi.data.repositories.recommendedshows.RecommendedShowsLastRequestStore
import app.tivi.data.repositories.recommendedshows.RecommendedShowsStore
import app.tivi.data.repositories.showimages.ShowImagesStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.domain.Interactor
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.threeten.bp.Duration
import javax.inject.Inject

class UpdateRecommendedShows @Inject constructor(
    private val recommendedShowsStore: RecommendedShowsStore,
    private val lastRequestStore: RecommendedShowsLastRequestStore,
    private val showsStore: ShowStore,
    private val showImagesStore: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers,
    private val traktManager: TraktManager
) : Interactor<UpdateRecommendedShows.Params>() {
    override suspend fun doWork(params: Params) {
        if (traktManager.state.first() != TraktAuthState.LOGGED_IN) {
            // If we're not logged in, we can't load the recommended shows
            return
        }
        withContext(dispatchers.io) {
            // Recommended fetcher does not support paging
            recommendedShowsStore.fetchCollection(0, forceFresh = params.forceRefresh) {
                // Refresh if our local data is over 3 hours old
                lastRequestStore.isRequestExpired(Duration.ofHours(3))
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
