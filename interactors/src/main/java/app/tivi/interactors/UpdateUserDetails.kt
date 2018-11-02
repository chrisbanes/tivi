/*
 * Copyright 2018 Google LLC
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

package app.tivi.interactors

import app.tivi.data.entities.TraktUser
import app.tivi.data.repositories.traktusers.TraktUsersRepository
import app.tivi.interactors.UpdateUserDetails.ExecuteParams
import app.tivi.interactors.UpdateUserDetails.Params
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.AppRxSchedulers
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class UpdateUserDetails @Inject constructor(
    private val schedulers: AppRxSchedulers,
    private val dispatchers: AppCoroutineDispatchers,
    private val repository: TraktUsersRepository
) : SubjectInteractor<Params, ExecuteParams, TraktUser>() {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend fun execute(params: Params, executeParams: ExecuteParams) {
        if (executeParams.forceLoad || repository.needUpdate(params.username)) {
            repository.updateUser(params.username)
        }
    }

    override fun createObservable(params: Params): Flowable<TraktUser> {
        return repository.observeUser(params.username)
                .subscribeOn(schedulers.io)
    }

    data class Params(val username: String)
    data class ExecuteParams(val forceLoad: Boolean)
}