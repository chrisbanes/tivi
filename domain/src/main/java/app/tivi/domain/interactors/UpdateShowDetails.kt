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
import app.tivi.data.repositories.shows.ShowLastRequestStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdateShowDetails.Params
import app.tivi.inject.ProcessLifetime
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import org.threeten.bp.Period
import javax.inject.Inject

class UpdateShowDetails @Inject constructor(
    private val showStore: ShowStore,
    private val lastRequestStore: ShowLastRequestStore,
    dispatchers: AppCoroutineDispatchers,
    @ProcessLifetime val processScope: CoroutineScope
) : Interactor<Params>() {
    override val scope: CoroutineScope = processScope + dispatchers.io

    override suspend fun doWork(params: Params) {
        showStore.fetch(params.showId, params.forceLoad) {
            // Refresh if our cached data is over 14 days old
            lastRequestStore.isRequestExpired(params.showId, Period.ofDays(14))
        }
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}
