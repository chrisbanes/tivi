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

package app.tivi.home.watched

import app.tivi.SharedElementHelper
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.datasources.trakt.WatchedShowsDataSource
import app.tivi.home.HomeNavigator
import app.tivi.interactors.UpdateWatchedShows
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.AppRxSchedulers
import app.tivi.util.EntryViewModel
import app.tivi.util.Logger
import app.tivi.util.NetworkDetector
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject

class WatchedShowsViewModel @Inject constructor(
    schedulers: AppRxSchedulers,
    dispatchers: AppCoroutineDispatchers,
    dataSource: WatchedShowsDataSource,
    private val interactor: UpdateWatchedShows,
    tmdbManager: TmdbManager,
    networkDetector: NetworkDetector,
    logger: Logger
) : EntryViewModel<WatchedShowEntryWithShow>(
        schedulers,
        dispatchers,
        dataSource,
        tmdbManager,
        networkDetector,
        logger
) {
    fun onUpClicked(navigator: HomeNavigator) {
        navigator.onUpClicked()
    }

    fun onItemClicked(item: WatchedShowEntryWithShow, navigator: HomeNavigator, sharedElements: SharedElementHelper?) {
        navigator.showShowDetails(item.show, sharedElements)
    }

    override suspend fun callRefresh() {
        withContext(interactor.dispatcher) {
            interactor(UpdateWatchedShows.Params(true))
        }
    }
}