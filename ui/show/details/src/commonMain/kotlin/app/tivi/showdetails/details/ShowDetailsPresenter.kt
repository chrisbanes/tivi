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
import app.tivi.screens.EpisodeDetailsScreen
import app.tivi.screens.ShowDetailsScreen
import app.tivi.screens.ShowSeasonsScreen
import app.tivi.util.launchOrThrow
import app.tivi.wrapEventSink
import co.touchlab.kermit.Logger
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
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
  updateShowDetails: Lazy<UpdateShowDetails>,
  observeShowDetails: Lazy<ObserveShowDetails>,
  updateRelatedShows: Lazy<UpdateRelatedShows>,
  observeRelatedShows: Lazy<ObserveRelatedShows>,
  updateShowSeasons: Lazy<UpdateShowSeasons>,
  observeShowSeasons: Lazy<ObserveShowSeasonsEpisodesWatches>,
  changeSeasonWatchedStatus: Lazy<ChangeSeasonWatchedStatus>,
  observeShowFollowStatus: Lazy<ObserveShowFollowStatus>,
  observeNextEpisodeToWatch: Lazy<ObserveShowNextEpisodeToWatch>,
  observeShowViewStats: Lazy<ObserveShowViewStats>,
  changeShowFollowStatus: Lazy<ChangeShowFollowStatus>,
  changeSeasonFollowStatus: Lazy<ChangeSeasonFollowStatus>,
) : Presenter<ShowDetailsUiState> {
  private val showId: Long get() = screen.id

  private val logger by lazy { Logger.withTag("ShowDetailsPresenter") }

  private val updateShowDetails by updateShowDetails
  private val observeShowDetails by observeShowDetails
  private val updateRelatedShows by updateRelatedShows
  private val observeRelatedShows by observeRelatedShows
  private val updateShowSeasons by updateShowSeasons
  private val observeShowSeasons by observeShowSeasons
  private val changeSeasonWatchedStatus by changeSeasonWatchedStatus
  private val observeShowFollowStatus by observeShowFollowStatus
  private val observeNextEpisodeToWatch by observeNextEpisodeToWatch
  private val observeShowViewStats by observeShowViewStats
  private val changeShowFollowStatus by changeShowFollowStatus
  private val changeSeasonFollowStatus by changeSeasonFollowStatus

  @Composable
  override fun present(): ShowDetailsUiState {
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

    fun handleException(t: Throwable) {
      logger.i(t) { "Error" }
      if (t !is IllegalArgumentException) {
        uiMessageManager.emitMessage(UiMessage(t))
      }
    }

    val eventSink: CoroutineScope.(ShowDetailsUiEvent) -> Unit = { event ->
      when (event) {
        is ShowDetailsUiEvent.ClearMessage -> {
          launchOrThrow {
            uiMessageManager.clearMessage(event.id)
          }
        }

        is ShowDetailsUiEvent.Refresh -> {
          launchOrThrow {
            updateShowDetails(
              UpdateShowDetails.Params(showId, event.fromUser),
            ).onFailure(::handleException)
          }
          launchOrThrow {
            updateRelatedShows(
              UpdateRelatedShows.Params(showId, event.fromUser),
            ).onFailure(::handleException)
          }
          launchOrThrow {
            updateShowSeasons(
              UpdateShowSeasons.Params(showId, event.fromUser),
            ).onFailure(::handleException)
          }
        }

        is ShowDetailsUiEvent.FollowSeason -> {
          launchOrThrow {
            changeSeasonFollowStatus(
              ChangeSeasonFollowStatus.Params(
                seasonId = event.seasonId,
                action = ChangeSeasonFollowStatus.Action.FOLLOW,
              ),
            ).onFailure(::handleException)
          }
        }

        is ShowDetailsUiEvent.MarkSeasonUnwatched -> {
          launchOrThrow {
            changeSeasonWatchedStatus(
              Params(event.seasonId, Action.UNWATCH),
            ).onFailure(::handleException)
          }
        }

        is ShowDetailsUiEvent.MarkSeasonWatched -> {
          launchOrThrow {
            changeSeasonWatchedStatus(
              Params(event.seasonId, Action.WATCH, event.onlyAired, event.date),
            ).onFailure(::handleException)
          }
        }

        ShowDetailsUiEvent.ToggleShowFollowed -> {
          launchOrThrow {
            changeShowFollowStatus(
              ChangeShowFollowStatus.Params(showId, TOGGLE),
            ).onFailure(::handleException)
          }
        }

        is ShowDetailsUiEvent.UnfollowPreviousSeasons -> {
          launchOrThrow {
            changeSeasonFollowStatus(
              ChangeSeasonFollowStatus.Params(
                seasonId = event.seasonId,
                action = ChangeSeasonFollowStatus.Action.IGNORE_PREVIOUS,
              ),
            ).onFailure { e ->
              logger.i(e) { "Error whilst calling ChangeSeasonFollowStatus" }
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        is ShowDetailsUiEvent.UnfollowSeason -> {
          launchOrThrow {
            changeSeasonFollowStatus(
              ChangeSeasonFollowStatus.Params(
                seasonId = event.seasonId,
                action = ChangeSeasonFollowStatus.Action.IGNORE,
              ),
            ).onFailure { handleException(it) }
          }
        }

        ShowDetailsUiEvent.NavigateBack -> navigator.pop()

        is ShowDetailsUiEvent.OpenSeason -> {
          navigator.goTo(ShowSeasonsScreen(showId, event.seasonId))
        }

        is ShowDetailsUiEvent.OpenShowDetails -> {
          navigator.goTo(ShowDetailsScreen(event.showId))
        }

        is ShowDetailsUiEvent.OpenEpisodeDetails -> {
          navigator.goTo(EpisodeDetailsScreen(event.episodeId))
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
      eventSink = wrapEventSink(eventSink),
    )
  }
}
