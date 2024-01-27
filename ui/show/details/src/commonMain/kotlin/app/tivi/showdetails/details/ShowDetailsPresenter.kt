// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.showdetails.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import app.tivi.common.compose.UiMessage
import app.tivi.common.compose.UiMessageManager
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.data.models.TiviShow
import app.tivi.domain.interactors.ChangeSeasonFollowStatus
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus.Action
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus.Params
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.ChangeShowFollowStatus.Action.TOGGLE
import app.tivi.domain.interactors.UpdateRelatedShows
import app.tivi.domain.interactors.UpdateShowDetails
import app.tivi.domain.interactors.UpdateShowSeasons
import app.tivi.domain.observers.ObserveRelatedShows
import app.tivi.domain.observers.ObserveShowDetails
import app.tivi.domain.observers.ObserveShowFollowStatus
import app.tivi.domain.observers.ObserveShowNextEpisodeToWatch
import app.tivi.domain.observers.ObserveShowSeasonsEpisodesWatches
import app.tivi.domain.observers.ObserveShowViewStats
import app.tivi.screens.ShowDetailsScreen
import app.tivi.screens.ShowSeasonsScreen
import app.tivi.util.Logger
import app.tivi.util.onException
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ShowDetailsUiPresenterFactory(
  private val presenterFactory: (ShowDetailsScreen, Navigator) -> ShowDetailsPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is ShowDetailsScreen -> presenterFactory(screen, navigator)
    else -> null
  }
}

@Inject
class ShowDetailsPresenter(
  @Assisted private val screen: ShowDetailsScreen,
  @Assisted private val navigator: Navigator,
  private val updateShowDetails: UpdateShowDetails,
  private val observeShowDetails: ObserveShowDetails,
  private val updateRelatedShows: UpdateRelatedShows,
  private val observeRelatedShows: ObserveRelatedShows,
  private val updateShowSeasons: UpdateShowSeasons,
  private val observeShowSeasons: ObserveShowSeasonsEpisodesWatches,
  private val changeSeasonWatchedStatus: ChangeSeasonWatchedStatus,
  private val observeShowFollowStatus: ObserveShowFollowStatus,
  private val observeNextEpisodeToWatch: ObserveShowNextEpisodeToWatch,
  private val observeShowViewStats: ObserveShowViewStats,
  private val changeShowFollowStatus: ChangeShowFollowStatus,
  private val changeSeasonFollowStatus: ChangeSeasonFollowStatus,
  private val logger: Logger,
) : Presenter<ShowDetailsUiState> {
  private val showId: Long get() = screen.id

  @Composable
  override fun present(): ShowDetailsUiState {
    val scope = rememberCoroutineScope()

    val uiMessageManager = remember { UiMessageManager() }

    val isFollowed by observeShowFollowStatus.flow.collectAsRetainedState(false)
    val show by observeShowDetails.flow.collectAsRetainedState(TiviShow.EMPTY_SHOW)
    val refreshing by produceState(false) {
      combine(
        updateShowDetails.inProgress,
        updateShowSeasons.inProgress,
        updateRelatedShows.inProgress,
        transform = { values -> values.any { it } },
      ).collect { value = it }
    }
    val relatedShows by observeRelatedShows.flow.collectAsRetainedState(emptyList())
    val nextEpisode by observeNextEpisodeToWatch.flow.collectAsRetainedState(null)
    val seasons by observeShowSeasons.flow.collectAsRetainedState(emptyList())
    val stats by observeShowViewStats.flow.collectAsRetainedState(null)
    val message by uiMessageManager.message.collectAsState(null)

    fun eventSink(event: ShowDetailsUiEvent) {
      when (event) {
        is ShowDetailsUiEvent.ClearMessage -> {
          scope.launch {
            uiMessageManager.clearMessage(event.id)
          }
        }

        is ShowDetailsUiEvent.Refresh -> {
          scope.launch {
            updateShowDetails(
              UpdateShowDetails.Params(showId, event.fromUser),
            ).onException { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
          scope.launch {
            updateRelatedShows(
              UpdateRelatedShows.Params(showId, event.fromUser),
            ).onException { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
          scope.launch {
            updateShowSeasons(
              UpdateShowSeasons.Params(showId, event.fromUser),
            ).onException { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        is ShowDetailsUiEvent.FollowSeason -> {
          scope.launch {
            changeSeasonFollowStatus(
              ChangeSeasonFollowStatus.Params(
                seasonId = event.seasonId,
                action = ChangeSeasonFollowStatus.Action.FOLLOW,
              ),
            ).onException { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        is ShowDetailsUiEvent.MarkSeasonUnwatched -> {
          scope.launch {
            changeSeasonWatchedStatus(
              Params(event.seasonId, Action.UNWATCH),
            ).onException { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        is ShowDetailsUiEvent.MarkSeasonWatched -> {
          scope.launch {
            changeSeasonWatchedStatus(
              Params(event.seasonId, Action.WATCHED, event.onlyAired, event.date),
            ).onException { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        ShowDetailsUiEvent.ToggleShowFollowed -> {
          scope.launch {
            changeShowFollowStatus(
              ChangeShowFollowStatus.Params(showId, TOGGLE),
            ).onException { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        is ShowDetailsUiEvent.UnfollowPreviousSeasons -> {
          scope.launch {
            changeSeasonFollowStatus(
              ChangeSeasonFollowStatus.Params(
                seasonId = event.seasonId,
                action = ChangeSeasonFollowStatus.Action.IGNORE_PREVIOUS,
              ),
            ).onException { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        is ShowDetailsUiEvent.UnfollowSeason -> {
          scope.launch {
            changeSeasonFollowStatus(
              ChangeSeasonFollowStatus.Params(
                seasonId = event.seasonId,
                action = ChangeSeasonFollowStatus.Action.IGNORE,
              ),
            ).onException { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }
        ShowDetailsUiEvent.NavigateBack -> navigator.pop()
        is ShowDetailsUiEvent.OpenSeason -> {
          navigator.goTo(ShowSeasonsScreen(showId, event.seasonId))
        }
        is ShowDetailsUiEvent.OpenShowDetails -> {
          navigator.goTo(ShowDetailsScreen(event.showId))
        }
      }
    }

    LaunchedEffect(Unit) {
      observeShowFollowStatus(ObserveShowFollowStatus.Params(showId))
      observeShowDetails(ObserveShowDetails.Params(showId))
      observeRelatedShows(ObserveRelatedShows.Params(showId))
      observeShowSeasons(ObserveShowSeasonsEpisodesWatches.Params(showId))
      observeNextEpisodeToWatch(ObserveShowNextEpisodeToWatch.Params(showId))
      observeShowViewStats(ObserveShowViewStats.Params(showId))

      eventSink(ShowDetailsUiEvent.Refresh(false))
    }

    return ShowDetailsUiState(
      isFollowed = isFollowed,
      show = show,
      relatedShows = relatedShows,
      nextEpisodeToWatch = nextEpisode,
      seasons = seasons,
      watchStats = stats,
      refreshing = refreshing,
      message = message,
      eventSink = ::eventSink,
    )
  }
}
