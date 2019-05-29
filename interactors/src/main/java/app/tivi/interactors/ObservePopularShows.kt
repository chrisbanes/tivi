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

import app.tivi.data.repositories.popularshows.PopularShowsRepository
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.extensions.emptyFlowableList
import app.tivi.util.AppRxSchedulers
import io.reactivex.Observable
import javax.inject.Inject

class ObservePopularShows @Inject constructor(
    private val schedulers: AppRxSchedulers,
    private val popularShowsRepository: PopularShowsRepository
) : SubjectInteractor<Unit, List<PopularEntryWithShow>>() {
    override fun createObservable(params: Unit): Observable<List<PopularEntryWithShow>> {
        return popularShowsRepository.observeForFlowable()
                .startWith(emptyFlowableList())
                .subscribeOn(schedulers.io)
                .toObservable()
    }
}
