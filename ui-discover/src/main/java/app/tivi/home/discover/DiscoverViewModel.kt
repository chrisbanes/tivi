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

package app.tivi.home.discover

import androidx.lifecycle.viewModelScope
import app.tivi.interactors.ObservePopularShows
import app.tivi.interactors.ObserveTrendingShows
import app.tivi.interactors.UpdatePopularShows
import app.tivi.interactors.UpdateTrendingShows
import app.tivi.interactors.launchInteractor
import app.tivi.tmdb.TmdbManager
import app.tivi.TiviMvRxViewModel
import app.tivi.interactors.launchObserve
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class DiscoverViewModel @AssistedInject constructor(
    @Assisted initialState: DiscoverViewState,
    private val updatePopularShows: UpdatePopularShows,
    observePopularShows: ObservePopularShows,
    private val updateTrendingShows: UpdateTrendingShows,
    observeTrendingShows: ObserveTrendingShows,
    tmdbManager: TmdbManager
) : TiviMvRxViewModel<DiscoverViewState>(initialState) {

    init {
        viewModelScope.launch {
            tmdbManager.imageProviderFlow.execute {
                copy(tmdbImageUrlProvider = it() ?: tmdbImageUrlProvider)
            }
        }

        viewModelScope.launchObserve(observeTrendingShows) {
            it.distinctUntilChanged().execute {
                copy(trendingItems = it() ?: emptyList())
            }
        }
        viewModelScope.launchInteractor(observeTrendingShows)

        viewModelScope.launchObserve(observePopularShows) {
            it.distinctUntilChanged().execute {
                copy(popularItems = it() ?: emptyList())
            }
        }
        viewModelScope.launchInteractor(observePopularShows)

        refresh()
    }

    fun refresh() {
        viewModelScope.launchInteractor(updatePopularShows,
                UpdatePopularShows.Params(UpdatePopularShows.Page.REFRESH)
        ) { setState { copy(popularRefreshing = !it) } }

        viewModelScope.launchInteractor(updateTrendingShows,
                UpdateTrendingShows.Params(UpdateTrendingShows.Page.REFRESH)
        ) { setState { copy(trendingRefreshing = !it) } }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: DiscoverViewState): DiscoverViewModel
    }

    companion object : MvRxViewModelFactory<DiscoverViewModel, DiscoverViewState> {
        override fun create(viewModelContext: ViewModelContext, state: DiscoverViewState): DiscoverViewModel? {
            val fragment: DiscoverFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.discoverViewModelFactory.create(state)
        }
    }
}
