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

import androidx.lifecycle.viewModelScope
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.inject.ProcessLifetime
import app.tivi.interactors.ObservePagedPopularShows
import app.tivi.interactors.UpdatePopularShows
import app.tivi.interactors.launchInteractor
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.EntryViewModel
import app.tivi.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PopularShowsViewModel @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val interactor: UpdatePopularShows,
    observePagedPopularShows: ObservePagedPopularShows,
    tmdbManager: TmdbManager,
    logger: Logger,
    @ProcessLifetime private val dataOperationScope: CoroutineScope
) : EntryViewModel<PopularEntryWithShow, ObservePagedPopularShows>(
        dispatchers,
        observePagedPopularShows,
        tmdbManager,
        logger
) {
    init {
        viewModelScope.launch {
            observePagedPopularShows(ObservePagedPopularShows.Params(pageListConfig, boundaryCallback))
        }
    }

    override suspend fun callLoadMore() = dataOperationScope.launchInteractor(
            interactor,
            UpdatePopularShows.Params(UpdatePopularShows.Page.NEXT_PAGE)
    )

    override suspend fun callRefresh() = dataOperationScope.launchInteractor(
            interactor,
            UpdatePopularShows.Params(UpdatePopularShows.Page.REFRESH)
    )
}