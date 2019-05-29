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

package app.tivi.interactors

import app.tivi.data.entities.TraktUser
import app.tivi.data.repositories.traktusers.TraktUsersRepository
import app.tivi.util.AppRxSchedulers
import io.reactivex.Observable
import javax.inject.Inject

class ObserveUserDetails @Inject constructor(
    private val schedulers: AppRxSchedulers,
    private val repository: TraktUsersRepository
) : SubjectInteractor<ObserveUserDetails.Params, TraktUser>() {
    override fun createObservable(params: Params): Observable<TraktUser> {
        return repository.observeUser(params.username)
                .subscribeOn(schedulers.io)
                .toObservable()
    }

    data class Params(val username: String)
}