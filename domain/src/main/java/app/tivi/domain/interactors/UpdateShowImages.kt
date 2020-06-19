/*
 * Copyright 2020 Google LLC
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

import app.tivi.data.fetchCollection
import app.tivi.data.repositories.showimages.ShowImagesLastRequestStore
import app.tivi.data.repositories.showimages.ShowImagesStore
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.Period
import javax.inject.Inject

class UpdateShowImages @Inject constructor(
    private val showImagesStore: ShowImagesStore,
    private val lastRequestStore: ShowImagesLastRequestStore,
    private val dispatchers: AppCoroutineDispatchers
) : Interactor<UpdateShowImages.Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            showImagesStore.fetchCollection(params.showId, params.forceLoad) {
                // Refresh if our local data is over 30 days old
                lastRequestStore.isRequestExpired(params.showId, Period.ofDays(30))
            }
        }
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}
