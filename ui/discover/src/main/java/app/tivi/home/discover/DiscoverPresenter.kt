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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.tivi.api.UiMessageManager
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.interactors.UpdatePopularShows
import app.tivi.domain.interactors.UpdateRecommendedShows
import app.tivi.domain.interactors.UpdateTrendingShows
import app.tivi.domain.observers.ObserveNextShowEpisodeToWatch
import app.tivi.domain.observers.ObservePopularShows
import app.tivi.domain.observers.ObserveRecommendedShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveTrendingShows
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.screens.AccountScreen
import app.tivi.screens.DiscoverScreen
import app.tivi.screens.EpisodeDetailsScreen
import app.tivi.screens.PopularShowsScreen
import app.tivi.screens.RecommendedShowsScreen
import app.tivi.screens.ShowDetailsScreen
import app.tivi.screens.ShowSeasonsScreen
import app.tivi.screens.TrendingShowsScreen
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class DiscoverUiPresenterFactory(
    private val presenterFactory: (Navigator) -> DiscoverPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is DiscoverScreen -> presenterFactory(navigator)
            else -> null
        }
    }
}

@Inject
class DiscoverPresenter(
    @Assisted private val navigator: Navigator,
    private val updatePopularShows: UpdatePopularShows,
    private val observePopularShows: ObservePopularShows,
    private val updateTrendingShows: UpdateTrendingShows,
    private val observeTrendingShows: ObserveTrendingShows,
    private val updateRecommendedShows: UpdateRecommendedShows,
    private val observeRecommendedShows: ObserveRecommendedShows,
    private val observeNextShowEpisodeToWatch: ObserveNextShowEpisodeToWatch,
    private val observeTraktAuthState: ObserveTraktAuthState,
    private val observeUserDetails: ObserveUserDetails,
    private val logger: Logger,
) : Presenter<DiscoverUiState> {

    @Composable
    override fun present(): DiscoverUiState {
        val scope = rememberCoroutineScope()

        val trendingLoadingState = remember { ObservableLoadingCounter() }
        val popularLoadingState = remember { ObservableLoadingCounter() }
        val recommendedLoadingState = remember { ObservableLoadingCounter() }
        val uiMessageManager = remember { UiMessageManager() }

        val trendingLoading by trendingLoadingState.observable.collectAsState(false)
        val trendingItems by observeTrendingShows.flow.collectAsState(emptyList())

        val popularItems by observePopularShows.flow.collectAsState(emptyList())
        val popularLoading by popularLoadingState.observable.collectAsState(false)

        val recommendedItems by observeRecommendedShows.flow.collectAsState(emptyList())
        val recommendedLoading by recommendedLoadingState.observable.collectAsState(false)

        val nextShow by observeNextShowEpisodeToWatch.flow.collectAsState(null)
        val authState by observeTraktAuthState.flow.collectAsState(TraktAuthState.LOGGED_OUT)
        val user by observeUserDetails.flow.collectAsState(null)

        val message by uiMessageManager.message.collectAsState(null)

        fun eventSink(event: DiscoverUiEvent) {
            when (event) {
                is DiscoverUiEvent.ClearMessage -> {
                    scope.launch {
                        uiMessageManager.clearMessage(event.id)
                    }
                }

                is DiscoverUiEvent.Refresh -> {
                    scope.launch {
                        updatePopularShows(
                            UpdatePopularShows.Params(
                                page = UpdatePopularShows.Page.REFRESH,
                                forceRefresh = event.fromUser,
                            ),
                        ).collectStatus(popularLoadingState, logger, uiMessageManager)
                    }
                    scope.launch {
                        updateTrendingShows(
                            UpdateTrendingShows.Params(
                                page = UpdateTrendingShows.Page.REFRESH,
                                forceRefresh = event.fromUser,
                            ),
                        ).collectStatus(trendingLoadingState, logger, uiMessageManager)
                    }
                    scope.launch {
                        updateRecommendedShows(
                            UpdateRecommendedShows.Params(forceRefresh = event.fromUser),
                        ).collectStatus(recommendedLoadingState, logger, uiMessageManager)
                    }
                }

                DiscoverUiEvent.OpenAccount -> navigator.goTo(AccountScreen)
                DiscoverUiEvent.OpenPopularShows -> navigator.goTo(PopularShowsScreen)
                DiscoverUiEvent.OpenRecommendedShows -> navigator.goTo(RecommendedShowsScreen)
                is DiscoverUiEvent.OpenShowDetails -> {
                    navigator.goTo(ShowDetailsScreen(event.showId))
                    if (event.seasonId != null) {
                        navigator.goTo(ShowSeasonsScreen(event.showId, event.seasonId))
                        if (event.episodeId != null) {
                            navigator.goTo(EpisodeDetailsScreen(event.episodeId))
                        }
                    }
                }

                DiscoverUiEvent.OpenTrendingShows -> navigator.goTo(TrendingShowsScreen)
            }
        }

        LaunchedEffect(Unit) {
            observeTrendingShows(ObserveTrendingShows.Params(10))
            observePopularShows(ObservePopularShows.Params(10))
            observeRecommendedShows(ObserveRecommendedShows.Params(10))
            observeNextShowEpisodeToWatch(Unit)
            observeTraktAuthState(Unit)
            observeUserDetails(ObserveUserDetails.Params("me"))

            eventSink(DiscoverUiEvent.Refresh(false))
        }

        LaunchedEffect(observeTraktAuthState) {
            // Each time the auth state changes, tickle the refresh signal...
            observeTraktAuthState.flow.collect {
                eventSink(DiscoverUiEvent.Refresh(false))
            }
        }

        return DiscoverUiState(
            user = user,
            authState = authState,
            trendingItems = trendingItems,
            trendingRefreshing = trendingLoading,
            popularItems = popularItems,
            popularRefreshing = popularLoading,
            recommendedItems = recommendedItems,
            recommendedRefreshing = recommendedLoading,
            nextEpisodeWithShowToWatch = nextShow,
            message = message,
            eventSink = ::eventSink,
        )
    }
}
