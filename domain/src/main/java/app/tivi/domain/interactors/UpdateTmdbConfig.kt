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

import app.tivi.domain.Interactor
import app.tivi.inject.ProcessLifetime
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppCoroutineDispatchers
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

class UpdateTmdbConfig @Inject constructor(
    private val tmdbManager: TmdbManager,
    dispatchers: AppCoroutineDispatchers,
    @ProcessLifetime val processScope: CoroutineScope
) : Interactor<Unit>() {
    override val scope: CoroutineScope = processScope + dispatchers.io

    override suspend fun doWork(params: Unit) {
        tmdbManager.refreshConfiguration()
    }
}
