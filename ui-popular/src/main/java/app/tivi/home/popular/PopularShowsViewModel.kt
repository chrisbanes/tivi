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

package app.tivi.home.popular

import androidx.hilt.lifecycle.ViewModelInject
import app.tivi.base.InvokeStatus
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.UpdatePopularShows
import app.tivi.domain.observers.ObservePagedPopularShows
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.EntryViewModel
import app.tivi.util.Logger
import kotlinx.coroutines.flow.Flow

class PopularShowsViewModel @ViewModelInject constructor(
    override val dispatchers: AppCoroutineDispatchers,
    override val pagingInteractor: ObservePagedPopularShows,
    private val interactor: UpdatePopularShows,
    override val logger: Logger,
    override val changeShowFollowStatus: ChangeShowFollowStatus
) : EntryViewModel<PopularEntryWithShow, ObservePagedPopularShows>() {
    init {
        pagingInteractor(ObservePagedPopularShows.Params(pageListConfig, boundaryCallback))

        launchObserves()

        refresh(false)
    }

    override fun callLoadMore(): Flow<InvokeStatus> {
        return interactor(UpdatePopularShows.Params(UpdatePopularShows.Page.NEXT_PAGE, true))
    }

    override fun callRefresh(fromUser: Boolean): Flow<InvokeStatus> {
        return interactor(UpdatePopularShows.Params(UpdatePopularShows.Page.REFRESH, fromUser))
    }
}
