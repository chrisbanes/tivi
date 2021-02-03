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
import app.tivi.ReduxViewModel
import app.tivi.domain.interactors.UpdatePopularShows
import app.tivi.domain.interactors.UpdateRecommendedShows
import app.tivi.domain.interactors.UpdateTrendingShows
import app.tivi.domain.observers.ObserveNextShowEpisodeToWatch
import app.tivi.domain.observers.ObservePopularShows
import app.tivi.domain.observers.ObserveRecommendedShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveTrendingShows
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectInto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DiscoverViewModel @Inject constructor(
    private val updatePopularShows: UpdatePopularShows,
    observePopularShows: ObservePopularShows,
    private val updateTrendingShows: UpdateTrendingShows,
    observeTrendingShows: ObserveTrendingShows,
    private val updateRecommendedShows: UpdateRecommendedShows,
    observeRecommendedShows: ObserveRecommendedShows,
    observeNextShowEpisodeToWatch: ObserveNextShowEpisodeToWatch,
    observeTraktAuthState: ObserveTraktAuthState,
    observeUserDetails: ObserveUserDetails
) : ReduxViewModel<DiscoverViewState>(
    DiscoverViewState()
) {
    private val trendingLoadingState = ObservableLoadingCounter()
    private val popularLoadingState = ObservableLoadingCounter()
    private val recommendedLoadingState = ObservableLoadingCounter()

    private val pendingActions = MutableSharedFlow<DiscoverAction>()

    init {
        viewModelScope.launch {
            trendingLoadingState.observable
                .collectAndSetState { copy(trendingRefreshing = it) }
        }

        viewModelScope.launch {
            popularLoadingState.observable
                .collectAndSetState { copy(popularRefreshing = it) }
        }

        viewModelScope.launch {
            recommendedLoadingState.observable
                .collectAndSetState { copy(recommendedRefreshing = it) }
        }

        viewModelScope.launch {
            observeTrendingShows.observe()
                .distinctUntilChanged()
                .collectAndSetState { copy(trendingItems = it) }
        }
        observeTrendingShows(ObserveTrendingShows.Params(10))

        viewModelScope.launch {
            observePopularShows.observe()
                .distinctUntilChanged()
                .collectAndSetState { copy(popularItems = it) }
        }
        observePopularShows(ObservePopularShows.Params(10))

        viewModelScope.launch {
            observeRecommendedShows.observe()
                .distinctUntilChanged()
                .collectAndSetState { copy(recommendedItems = it) }
        }
        observeRecommendedShows(ObserveRecommendedShows.Params(10))

        viewModelScope.launch {
            observeNextShowEpisodeToWatch.observe()
                .distinctUntilChanged()
                .collectAndSetState { copy(nextEpisodeWithShowToWatched = it) }
        }
        observeNextShowEpisodeToWatch(Unit)

        viewModelScope.launch {
            observeTraktAuthState.observe()
                .distinctUntilChanged()
                .onEach { if (it == TraktAuthState.LOGGED_IN) refresh(false) }
                .collectAndSetState { copy(authState = it) }
        }
        observeTraktAuthState(Unit)

        viewModelScope.launch {
            observeUserDetails.observe().collectAndSetState { copy(user = it) }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    RefreshAction -> refresh(true)
                }
            }
        }

        refresh(false)
    }

    private fun refresh(fromUser: Boolean) {
        viewModelScope.launch {
            updatePopularShows(UpdatePopularShows.Params(UpdatePopularShows.Page.REFRESH, fromUser))
                .collectInto(popularLoadingState)
        }
        viewModelScope.launch {
            updateTrendingShows(UpdateTrendingShows.Params(UpdateTrendingShows.Page.REFRESH, fromUser))
                .collectInto(trendingLoadingState)
        }
        viewModelScope.launch {
            updateRecommendedShows(UpdateRecommendedShows.Params(forceRefresh = fromUser))
                .collectInto(recommendedLoadingState)
        }
    }

    fun submitAction(action: DiscoverAction) {
        viewModelScope.launch {
            pendingActions.emit(action)
        }
    }
}
