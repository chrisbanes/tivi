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
import app.tivi.extensions.parallelForEach
import app.tivi.inject.ProcessLifetime
import app.tivi.util.AppCoroutineDispatchers
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import org.threeten.bp.Period

class UpdateRelatedShows @Inject constructor(
    private val relatedShowsStore: RelatedShowsStore,
    private val lastRequestStore: RelatedShowsLastRequestStore,
    private val showsStore: ShowStore,
    private val showImagesStore: ShowImagesStore,
    dispatchers: AppCoroutineDispatchers,
    @ProcessLifetime val processScope: CoroutineScope
) : Interactor<Params>() {
    override val scope: CoroutineScope = processScope + dispatchers.io

    override suspend fun doWork(params: Params) {
        relatedShowsStore.fetchCollection(params.showId, forceFresh = params.forceLoad) {
            // Refresh if our local data is over 28 days old
            lastRequestStore.isRequestExpired(params.showId, Period.ofDays(28))
        }.parallelForEach {
            showsStore.fetch(it.showId)
            showImagesStore.fetchCollection(it.showId)
        }
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}
