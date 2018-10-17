/*
 * Copyright 2017 Google, Inc.
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

import app.tivi.SharedElementHelper
import app.tivi.data.resultentities.TrendingEntryWithShow
import app.tivi.home.HomeNavigator
import app.tivi.interactors.UpdateTrendingShows
import app.tivi.interactors.launchInteractor
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.AppRxSchedulers
import app.tivi.util.EntryViewModel
import app.tivi.util.Logger
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class TrendingShowsViewModel @Inject constructor(
    schedulers: AppRxSchedulers,
    dispatchers: AppCoroutineDispatchers,
    private val interactor: UpdateTrendingShows,
    tmdbManager: TmdbManager,
    logger: Logger
) : EntryViewModel<TrendingEntryWithShow>(
        schedulers,
        dispatchers,
        interactor.dataSourceFactory(),
        tmdbManager,
        logger
) {
    fun onUpClicked(navigator: HomeNavigator) {
        navigator.onUpClicked()
    }

    fun onItemClicked(item: TrendingEntryWithShow, navigator: HomeNavigator, sharedElements: SharedElementHelper?) {
        navigator.showShowDetails(item.show, sharedElements)
    }

    override suspend fun callLoadMore() = coroutineScope {
        launchInteractor(interactor, UpdateTrendingShows.ExecuteParams(UpdateTrendingShows.Page.NEXT_PAGE)).join()
    }

    override suspend fun callRefresh() = coroutineScope {
        launchInteractor(interactor, UpdateTrendingShows.ExecuteParams(UpdateTrendingShows.Page.REFRESH)).join()
    }
}
