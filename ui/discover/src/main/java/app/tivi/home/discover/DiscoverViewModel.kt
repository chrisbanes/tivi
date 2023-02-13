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
import app.tivi.api.UiMessageManager
import app.tivi.domain.interactors.UpdatePopularShows
import app.tivi.domain.interactors.UpdateRecommendedShows
import app.tivi.domain.interactors.UpdateTrendingShows
import app.tivi.domain.observers.ObserveNextShowEpisodeToWatch
import app.tivi.domain.observers.ObservePopularShows
import app.tivi.domain.observers.ObserveRecommendedShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveTrendingShows
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.extensions.combine
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class DiscoverViewModel(
    private val updatePopularShows: UpdatePopularShows,
    observePopularShows: ObservePopularShows,
    private val updateTrendingShows: UpdateTrendingShows,
    observeTrendingShows: ObserveTrendingShows,
    private val updateRecommendedShows: UpdateRecommendedShows,
    observeRecommendedShows: ObserveRecommendedShows,
    observeNextShowEpisodeToWatch: ObserveNextShowEpisodeToWatch,
    observeTraktAuthState: ObserveTraktAuthState,
    observeUserDetails: ObserveUserDetails,
    private val logger: Logger,
) : ViewModel() {
    private val trendingLoadingState = ObservableLoadingCounter()
    private val popularLoadingState = ObservableLoadingCounter()
    private val recommendedLoadingState = ObservableLoadingCounter()
    private val uiMessageManager = UiMessageManager()

    val state: StateFlow<DiscoverViewState> = combine(
        trendingLoadingState.observable,
        popularLoadingState.observable,
        recommendedLoadingState.observable,
        observeTrendingShows.flow,
        observePopularShows.flow,
        observeRecommendedShows.flow,
        observeNextShowEpisodeToWatch.flow,
        observeTraktAuthState.flow,
        observeUserDetails.flow,
        uiMessageManager.message,
    ) { trendingLoad, popularLoad, recommendLoad, trending, popular, recommended, nextShow,
            authState, user, message,
        ->
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
            message = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = DiscoverViewState.Empty,
    )

    init {
        observeTrendingShows(ObserveTrendingShows.Params(10))
        observePopularShows(ObservePopularShows.Params(10))
        observeRecommendedShows(ObserveRecommendedShows.Params(10))
        observeNextShowEpisodeToWatch(Unit)
        observeTraktAuthState(Unit)
        observeUserDetails(ObserveUserDetails.Params("me"))

        viewModelScope.launch {
            // Each time the auth state changes, refresh...
            observeTraktAuthState.flow.collect { refresh(false) }
        }
    }

    fun refresh(fromUser: Boolean = true) {
        viewModelScope.launch {
            updatePopularShows(
                UpdatePopularShows.Params(UpdatePopularShows.Page.REFRESH, fromUser),
            ).collectStatus(popularLoadingState, logger, uiMessageManager)
        }
        viewModelScope.launch {
            updateTrendingShows(
                UpdateTrendingShows.Params(UpdateTrendingShows.Page.REFRESH, fromUser),
            ).collectStatus(trendingLoadingState, logger, uiMessageManager)
        }
        viewModelScope.launch {
            updateRecommendedShows(
                UpdateRecommendedShows.Params(forceRefresh = fromUser),
            ).collectStatus(recommendedLoadingState, logger, uiMessageManager)
        }
    }

    fun clearMessage(id: Long) {
        viewModelScope.launch {
            uiMessageManager.clearMessage(id)
        }
    }
}
