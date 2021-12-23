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
import app.tivi.data.repositories.relatedshows.RelatedShowsStore
import app.tivi.data.repositories.showimages.ShowImagesStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdateRelatedShows.Params
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateRelatedShows @Inject constructor(
    private val relatedShowsStore: RelatedShowsStore,
    private val showsStore: ShowStore,
    private val showImagesStore: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers,
    private val logger: Logger,
) : Interactor<Params>() {
    override suspend fun doWork(params: Params) = withContext(dispatchers.io) {
        relatedShowsStore.fetch(params.showId, params.forceLoad).forEach {
            try {
                showsStore.fetch(it.otherShowId)
            } catch (t: Throwable) {
                logger.e(t, "Error while show info: ${it.otherShowId}")
            }
            try {
                showImagesStore.fetch(it.otherShowId)
            } catch (t: Throwable) {
                logger.e(t, "Error while fetching images for show: ${it.showId}")
            }
        }
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}
