/*
 * Copyright 2017 Google LLC
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

package app.tivi.home.recommended

import androidx.hilt.lifecycle.ViewModelInject
import app.tivi.data.resultentities.RecommendedEntryWithShow
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.UpdateRecommendedShows
import app.tivi.domain.interactors.UpdateRecommendedShows.Page.NEXT_PAGE
import app.tivi.domain.interactors.UpdateRecommendedShows.Page.REFRESH
import app.tivi.domain.observers.ObservePagedRecommendedShows
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.EntryViewModel
import app.tivi.util.Logger

class RecommendedShowsViewModel @ViewModelInject constructor(
    override val dispatchers: AppCoroutineDispatchers,
    override val pagingInteractor: ObservePagedRecommendedShows,
    private val interactor: UpdateRecommendedShows,
    override val logger: Logger,
    override val changeShowFollowStatus: ChangeShowFollowStatus
) : EntryViewModel<RecommendedEntryWithShow, ObservePagedRecommendedShows>() {
    init {
        pagingInteractor(ObservePagedRecommendedShows.Params(pageListConfig, boundaryCallback))

        launchObserves()

        refresh(false)
    }

    override fun callLoadMore() = interactor(UpdateRecommendedShows.Params(NEXT_PAGE, true))

    override fun callRefresh(fromUser: Boolean) = interactor(UpdateRecommendedShows.Params(REFRESH, fromUser))
}
