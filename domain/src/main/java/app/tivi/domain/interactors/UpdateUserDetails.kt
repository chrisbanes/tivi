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

import app.tivi.data.repositories.traktusers.TraktUsersRepository
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdateUserDetails.Params
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateUserDetails @Inject constructor(
    private val repository: TraktUsersRepository,
    private val dispatchers: AppCoroutineDispatchers
) : Interactor<Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            if (params.forceLoad || repository.needUpdate(params.username)) {
                repository.updateUser(params.username)
            }
        }
    }

    data class Params(val username: String, val forceLoad: Boolean)
}
