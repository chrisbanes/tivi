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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import app.tivi.AppNavigator
import app.tivi.ReduxViewModel
import app.tivi.domain.interactors.UpdatePopularShows
import app.tivi.domain.interactors.UpdateRecommendedShows
import app.tivi.domain.interactors.UpdateTrendingShows
import app.tivi.domain.invoke
import app.tivi.domain.observers.ObserveNextShowEpisodeToWatch
import app.tivi.domain.observers.ObservePopularShows
import app.tivi.domain.observers.ObserveRecommendedShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveTrendingShows
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectInto
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Provider

internal class DiscoverViewModel @ViewModelInject constructor(
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
) : ReduxViewModel<DiscoverViewState>(
    DiscoverViewState()
) {
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

        viewModelScope.launch {
            observeTrendingShows.observe()
                .distinctUntilChanged()
                .execute { copy(trendingItems = it() ?: emptyList()) }
        }
        observeTrendingShows(ObserveTrendingShows.Params(15))

        viewModelScope.launch {
            observePopularShows.observe()
                .distinctUntilChanged()
                .execute {
                    copy(popularItems = it() ?: emptyList())
                }
        }
        observePopularShows()

        viewModelScope.launch {
            observeRecommendedShows.observe()
                .distinctUntilChanged()
                .execute { copy(recommendedItems = it() ?: emptyList()) }
        }
        observeRecommendedShows()

        viewModelScope.launch {
            observeNextShowEpisodeToWatch.observe()
                .distinctUntilChanged()
                .execute { copy(nextEpisodeWithShowToWatched = it()) }
        }
        observeNextShowEpisodeToWatch()

        viewModelScope.launch {
            observeTraktAuthState.observe()
                .distinctUntilChanged()
                .onEach { if (it == TraktAuthState.LOGGED_IN) refresh(false) }
                .execute { copy(authState = it() ?: TraktAuthState.LOGGED_OUT) }
        }
        observeTraktAuthState()

        viewModelScope.launch {
            observeUserDetails.observe()
                .execute { copy(user = it()) }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        refresh(false)
    }

    fun refresh() = refresh(true)

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
            updateRecommendedShows(UpdateRecommendedShows.Params(UpdateRecommendedShows.Page.REFRESH, fromUser))
                .collectInto(recommendedLoadingState)
        }
    }
}
