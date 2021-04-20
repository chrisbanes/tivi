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
import app.tivi.data.repositories.showimages.ShowImagesStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.data.repositories.watchedshows.WatchedShowsStore
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateWatchedShows @Inject constructor(
    private val watchedShowsStore: WatchedShowsStore,
    private val showsStore: ShowStore,
    private val showImagesStore: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers,
    private val logger: Logger,
) : Interactor<UpdateWatchedShows.Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            watchedShowsStore.fetch(Unit, params.forceRefresh).forEach {
                ensureActive()
                showsStore.fetch(it.showId)

                ensureActive()
                try {
                    showImagesStore.fetch(it.showId)
                } catch (t: Throwable) {
                    logger.e("Error while fetching images for show: ${it.showId}", t)
                }
            }
        }
    }

    data class Params(val forceRefresh: Boolean)
}
