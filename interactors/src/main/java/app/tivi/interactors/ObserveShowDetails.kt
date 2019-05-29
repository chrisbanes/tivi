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

import app.tivi.data.entities.TiviShow
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.util.AppRxSchedulers
import io.reactivex.Observable
import javax.inject.Inject

class ObserveShowDetails @Inject constructor(
    private val showRepository: ShowRepository,
    private val schedulers: AppRxSchedulers
) : SubjectInteractor<ObserveShowDetails.Params, TiviShow>() {
    override fun createObservable(params: Params): Observable<TiviShow> {
        return showRepository.observeShow(params.showId)
                .subscribeOn(schedulers.io)
                .toObservable()
    }

    data class Params(val showId: Long)
}