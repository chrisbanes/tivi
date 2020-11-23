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
import app.tivi.domain.interactors.UpdateRecommendedShows.Params
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlinx.coroutines.withContext
import org.threeten.bp.Duration
import javax.inject.Inject

class UpdateRecommendedShows @Inject constructor(
    private val RecommendedShowsStore: RecommendedShowsStore,
    private val lastRequestStore: RecommendedShowsLastRequestStore,
    private val showStore: ShowStore,
    private val showImagesStore: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers,
    private val traktManager: TraktManager,
    private val logger: Logger,
) : Interactor<Params>() {
    override suspend fun doWork(params: Params) {
        // If we're not logged in, we can't load the recommended shows
        if (traktManager.state.value != TraktAuthState.LOGGED_IN) return

        withContext(dispatchers.io) {
            RecommendedShowsStore.fetchCollection(0, forceFresh = params.forceRefresh) {
                // Refresh if our local data is over 3 hours old
                lastRequestStore.isRequestExpired(Duration.ofHours(3))
            }.forEach {
                showStore.fetch(it.showId)
                try {
                    showImagesStore.fetchCollection(it.showId)
                } catch (t: Throwable) {
                    logger.e("Error while fetching image", t)
                }
            }
        }
    }

    data class Params(val forceRefresh: Boolean = false)
}
