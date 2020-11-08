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

package app.tivi.home.trending

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import app.tivi.base.InvokeStatus
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.UpdateTrendingShows
import app.tivi.domain.interactors.UpdateTrendingShows.Page.NEXT_PAGE
import app.tivi.domain.interactors.UpdateTrendingShows.Page.REFRESH
import app.tivi.domain.observers.ObservePagedTrendingShows
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlinx.coroutines.flow.Flow

@Suppress("unused")
class TrendingShowsViewModel @ViewModelInject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    pagingInteractor: ObservePagedTrendingShows,
    private val interactor: UpdateTrendingShows,
    private val logger: Logger,
    private val changeShowFollowStatus: ChangeShowFollowStatus
) : ViewModel() {
    init {
        pagingInteractor(ObservePagedTrendingShows.Params(PAGING_CONFIG))

        refresh(false)
    }

    private fun callLoadMore(): Flow<InvokeStatus> {
        return interactor(UpdateTrendingShows.Params(NEXT_PAGE, true))
    }

    private fun refresh(fromUser: Boolean): Flow<InvokeStatus> {
        return interactor(UpdateTrendingShows.Params(REFRESH, fromUser))
    }

    companion object {
        val PAGING_CONFIG = PagingConfig(
            pageSize = 21 * 3,
            prefetchDistance = 21,
            enablePlaceholders = false
        )
    }
}
