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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tivi.common.compose.combine
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
import kotlinx.coroutines.flow.Flow
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
) : ViewModel() {
    private val trendingLoadingState = ObservableLoadingCounter()
    private val popularLoadingState = ObservableLoadingCounter()
    private val recommendedLoadingState = ObservableLoadingCounter()

    private val pendingActions = MutableSharedFlow<DiscoverAction>()

    val state: Flow<DiscoverViewState> = combine(
        trendingLoadingState.observable,
        popularLoadingState.observable,
        recommendedLoadingState.observable,
        observeTrendingShows.observe().distinctUntilChanged(),
        observePopularShows.observe().distinctUntilChanged(),
        observeRecommendedShows.observe().distinctUntilChanged(),
        observeNextShowEpisodeToWatch.observe().distinctUntilChanged(),
        observeTraktAuthState.observe().distinctUntilChanged()
            .onEach { if (it == TraktAuthState.LOGGED_IN) refresh(false) },
        observeUserDetails.observe(),
    ) { trendingLoad, popularLoad, recommendLoad, trending, popular, recommended,
        nextShow, authState, user ->
        DiscoverViewState(
            user = user,
            authState = authState,
            trendingItems = trending,
            trendingRefreshing = trendingLoad,
            popularItems = popular,
            popularRefreshing = popularLoad,
            recommendedItems = recommended,
            recommendedRefreshing = recommendLoad,
            nextEpisodeWithShowToWatched = nextShow,
        )
    }

    init {
        observeTrendingShows(ObserveTrendingShows.Params(10))
        observePopularShows(ObservePopularShows.Params(10))
        observeRecommendedShows(ObserveRecommendedShows.Params(10))
        observeNextShowEpisodeToWatch(Unit)
        observeTraktAuthState(Unit)
        observeUserDetails(ObserveUserDetails.Params("me"))

        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    DiscoverAction.RefreshAction -> refresh(true)
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
