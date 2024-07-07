// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.discover

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import app.tivi.common.compose.UiMessage
import app.tivi.common.compose.UiMessageManager
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.interactors.UpdatePopularShows
import app.tivi.domain.interactors.UpdateRecommendedShows
import app.tivi.domain.interactors.UpdateTrendingShows
import app.tivi.domain.observers.ObserveNextShowEpisodesToWatch
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
import app.tivi.screens.TrendingShowsScreen
import app.tivi.util.Logger
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
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
  ): Presenter<*>? = when (screen) {
    is DiscoverScreen -> presenterFactory(navigator)
    else -> null
  }
}

@Inject
class DiscoverPresenter(
  @Assisted private val navigator: Navigator,
  private val updatePopularShows: Lazy<UpdatePopularShows>,
  private val observePopularShows: Lazy<ObservePopularShows>,
  private val updateTrendingShows: Lazy<UpdateTrendingShows>,
  private val observeTrendingShows: Lazy<ObserveTrendingShows>,
  private val updateRecommendedShows: Lazy<UpdateRecommendedShows>,
  private val observeRecommendedShows: Lazy<ObserveRecommendedShows>,
  private val observeNextShowEpisodesToWatch: Lazy<ObserveNextShowEpisodesToWatch>,
  private val observeTraktAuthState: Lazy<ObserveTraktAuthState>,
  private val observeUserDetails: Lazy<ObserveUserDetails>,
  private val logger: Logger,
) : Presenter<DiscoverUiState> {

  @Composable
  override fun present(): DiscoverUiState {
    val scope = rememberCoroutineScope()
    val uiMessageManager = remember { UiMessageManager() }

    val trendingItems by observeTrendingShows.value.flow.collectAsRetainedState(emptyList())
    val trendingLoading by updateTrendingShows.value.inProgress.collectAsState(false)

    val popularItems by observePopularShows.value.flow.collectAsRetainedState(emptyList())
    val popularLoading by updatePopularShows.value.inProgress.collectAsState(false)

    val recommendedItems by observeRecommendedShows.value.flow.collectAsRetainedState(emptyList())
    val recommendedLoading by updateRecommendedShows.value.inProgress.collectAsState(false)

    val nextEpisodesToWatch by observeNextShowEpisodesToWatch.value.flow.collectAsRetainedState(emptyList())
    val authState by observeTraktAuthState.value.flow.collectAsRetainedState(TraktAuthState.LOGGED_OUT)
    val user by observeUserDetails.value.flow.collectAsRetainedState(null)

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
            updatePopularShows.value.invoke(
              UpdatePopularShows.Params(
                page = UpdatePopularShows.Page.REFRESH,
                isUserInitiated = event.fromUser,
              ),
            ).onFailure { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
          scope.launch {
            updateTrendingShows.value.invoke(
              UpdateTrendingShows.Params(
                page = UpdateTrendingShows.Page.REFRESH,
                isUserInitiated = event.fromUser,
              ),
            ).onFailure { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
          if (authState == TraktAuthState.LOGGED_IN) {
            scope.launch {
              updateRecommendedShows.value.invoke(
                UpdateRecommendedShows.Params(isUserInitiated = event.fromUser),
              ).onFailure { e ->
                logger.i(e)
                uiMessageManager.emitMessage(UiMessage(e))
              }
            }
          }
        }

        DiscoverUiEvent.OpenAccount -> navigator.goTo(AccountScreen)
        DiscoverUiEvent.OpenPopularShows -> navigator.goTo(PopularShowsScreen)
        DiscoverUiEvent.OpenRecommendedShows -> navigator.goTo(RecommendedShowsScreen)
        is DiscoverUiEvent.OpenShowDetails -> navigator.goTo(ShowDetailsScreen(event.showId))
        DiscoverUiEvent.OpenTrendingShows -> navigator.goTo(TrendingShowsScreen)
        is DiscoverUiEvent.OpenEpisodeDetails -> {
          navigator.goTo(EpisodeDetailsScreen(event.episodeId))
        }
      }
    }

    LaunchedEffect(Unit) {
      observeTrendingShows.value.invoke(ObserveTrendingShows.Params(10))
      observePopularShows.value.invoke(ObservePopularShows.Params(10))
      observeRecommendedShows.value.invoke(ObserveRecommendedShows.Params(10))
      observeNextShowEpisodesToWatch.value.invoke(
        ObserveNextShowEpisodesToWatch.Params(followedOnly = true, limit = 6),
      )
      observeTraktAuthState.value.invoke(Unit)
      observeUserDetails.value.invoke(ObserveUserDetails.Params("me"))
    }

    LaunchedEffect(authState) {
      eventSink(DiscoverUiEvent.Refresh(false))
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
      nextEpisodesToWatch = nextEpisodesToWatch,
      message = message,
      eventSink = ::eventSink,
    )
  }
}
