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
import app.tivi.data.repositories.relatedshows.RelatedShowsLastRequestStore
import app.tivi.data.repositories.relatedshows.RelatedShowsStore
import app.tivi.data.repositories.showimages.ShowImagesStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdateRelatedShows.Params
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.threeten.bp.Period
import javax.inject.Inject

class UpdateRelatedShows @Inject constructor(
    private val relatedShowsStore: RelatedShowsStore,
    private val lastRequestStore: RelatedShowsLastRequestStore,
    private val showsStore: ShowStore,
    private val showImagesStore: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers
) : Interactor<Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            relatedShowsStore.fetchCollection(params.showId, params.forceLoad) {
                // Refresh if our local data is over 28 days old
                lastRequestStore.isRequestExpired(params.showId, Period.ofDays(28))
            }.asFlow().collect {
                showsStore.fetch(it.otherShowId)
                showImagesStore.fetchCollection(it.otherShowId)
            }
        }
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}
