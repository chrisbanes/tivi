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
import app.tivi.interactors.UpdatePopularShows
import app.tivi.interactors.UpdateTrendingShows
import app.tivi.interactors.launchInteractor
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppRxSchedulers
import app.tivi.util.RxLoadingCounter
import app.tivi.util.TiviMvRxViewModel
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class DiscoverViewModel @AssistedInject constructor(
    @Assisted initialState: DiscoverViewState,
    schedulers: AppRxSchedulers,
    private val updatePopularShows: UpdatePopularShows,
    private val updateTrendingShows: UpdateTrendingShows,
    tmdbManager: TmdbManager
) : TiviMvRxViewModel<DiscoverViewState>(initialState) {
    private val loadingState = RxLoadingCounter()

    init {
        tmdbManager.imageProviderObservable
                .execute { copy(tmdbImageUrlProvider = it() ?: tmdbImageUrlProvider) }

        loadingState.observable
                .execute { copy(isLoading = it() ?: false) }

        updateTrendingShows.observe()
                .toObservable()
                .subscribeOn(schedulers.io)
                .execute { copy(trendingItems = it() ?: emptyList()) }

        updatePopularShows.observe()
                .toObservable()
                .subscribeOn(schedulers.io)
                .execute { copy(popularItems = it() ?: emptyList()) }

        refresh()
    }

    fun refresh() {
        loadingState.addLoader()
        viewModelScope.launchInteractor(
                updatePopularShows,
                UpdatePopularShows.ExecuteParams(UpdatePopularShows.Page.REFRESH)
        ).invokeOnCompletion { loadingState.removeLoader() }

        loadingState.addLoader()
        viewModelScope.launchInteractor(
                updateTrendingShows,
                UpdateTrendingShows.ExecuteParams(UpdateTrendingShows.Page.REFRESH)
        ).invokeOnCompletion { loadingState.removeLoader() }
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
