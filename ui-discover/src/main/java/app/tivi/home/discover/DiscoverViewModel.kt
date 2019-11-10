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
import app.tivi.AppNavigator
import app.tivi.TiviMvRxViewModel
import app.tivi.domain.interactors.UpdatePopularShows
import app.tivi.domain.interactors.UpdateRecommendedShows
import app.tivi.domain.interactors.UpdateTrendingShows
import app.tivi.domain.invoke
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObserveNextShowEpisodeToWatch
import app.tivi.domain.observers.ObservePopularShows
import app.tivi.domain.observers.ObserveRecommendedShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveTrendingShows
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectFrom
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Provider

class DiscoverViewModel @AssistedInject constructor(
    @Assisted initialState: DiscoverViewState,
    private val updatePopularShows: UpdatePopularShows,
    observePopularShows: ObservePopularShows,
    private val updateTrendingShows: UpdateTrendingShows,
    observeTrendingShows: ObserveTrendingShows,
    private val updateRecommendedShows: UpdateRecommendedShows,
    observeRecommendedShows: ObserveRecommendedShows,
    observeNextShowEpisodeToWatch: ObserveNextShowEpisodeToWatch,
    observeTraktAuthState: ObserveTraktAuthState,
    observeUserDetails: ObserveUserDetails,
    private val appNavigator: Provider<AppNavigator>
) : TiviMvRxViewModel<DiscoverViewState>(initialState) {
    private val trendingLoadingState = ObservableLoadingCounter()
    private val popularLoadingState = ObservableLoadingCounter()
    private val recommendedLoadingState = ObservableLoadingCounter()

    init {
        viewModelScope.launch {
            trendingLoadingState.observable.collect { active ->
                setState { copy(trendingRefreshing = active) }
            }
        }

        viewModelScope.launch {
            popularLoadingState.observable.collect { active ->
                setState { copy(popularRefreshing = active) }
            }
        }

        viewModelScope.launch {
            recommendedLoadingState.observable.collect { active ->
                setState { copy(recommendedRefreshing = active) }
            }
        }

        viewModelScope.launchObserve(observeTrendingShows) {
            it.distinctUntilChanged().execute {
                copy(trendingItems = it() ?: emptyList())
            }
        }
        observeTrendingShows()

        viewModelScope.launchObserve(observePopularShows) {
            it.distinctUntilChanged().execute {
                copy(popularItems = it() ?: emptyList())
            }
        }
        observePopularShows()

        viewModelScope.launchObserve(observeRecommendedShows) {
            it.distinctUntilChanged().execute {
                copy(recommendedItems = it() ?: emptyList())
            }
        }
        observeRecommendedShows()

        viewModelScope.launchObserve(observeNextShowEpisodeToWatch) {
            it.distinctUntilChanged().execute {
                copy(nextEpisodeWithShowToWatched = it())
            }
        }
        observeNextShowEpisodeToWatch()

        viewModelScope.launchObserve(observeTraktAuthState) { flow ->
            flow.distinctUntilChanged().onEach {
                if (it == TraktAuthState.LOGGED_IN) {
                    refresh(false)
                }
            }.execute {
                copy(authState = it() ?: TraktAuthState.LOGGED_OUT)
            }
        }
        observeTraktAuthState()

        viewModelScope.launchObserve(observeUserDetails) {
            it.execute { copy(user = it()) }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        refresh(false)
    }

    fun onLoginClicked() {
        appNavigator.get().startLogin()
    }

    fun refresh() = refresh(true)

    private fun refresh(fromUser: Boolean) {
        updatePopularShows(UpdatePopularShows.Params(UpdatePopularShows.Page.REFRESH, fromUser)).also {
            viewModelScope.launch {
                popularLoadingState.collectFrom(it)
            }
        }
        updateTrendingShows(UpdateTrendingShows.Params(UpdateTrendingShows.Page.REFRESH, fromUser)).also {
            viewModelScope.launch {
                trendingLoadingState.collectFrom(it)
            }
        }
        updateRecommendedShows(UpdateRecommendedShows.Params(UpdateRecommendedShows.Page.REFRESH, fromUser)).also {
            viewModelScope.launch {
                recommendedLoadingState.collectFrom(it)
            }
        }
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
