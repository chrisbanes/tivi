// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.episodedetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.Snapshot
import app.tivi.common.compose.UiMessage
import app.tivi.common.compose.UiMessageManager
import app.tivi.domain.interactors.RemoveEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatches
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeWatches
import app.tivi.domain.observers.ObserveShowDetailsForEpisodeId
import app.tivi.screens.EpisodeDetailsScreen
import app.tivi.screens.EpisodeTrackScreen
import app.tivi.screens.ShowDetailsScreen
import app.tivi.screens.ShowSeasonsScreen
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
class EpisodeDetailsUiPresenterFactory(
  private val presenterFactory: (EpisodeDetailsScreen, Navigator) -> EpisodeDetailsPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is EpisodeDetailsScreen -> presenterFactory(screen, navigator)
    else -> null
  }
}

@Inject
class EpisodeDetailsPresenter(
  @Assisted private val screen: EpisodeDetailsScreen,
  @Assisted private val navigator: Navigator,
  private val updateEpisodeDetails: Lazy<UpdateEpisodeDetails>,
  private val observeShowDetailsForEpisodeId: Lazy<ObserveShowDetailsForEpisodeId>,
  private val observeEpisodeDetails: Lazy<ObserveEpisodeDetails>,
  private val observeEpisodeWatches: Lazy<ObserveEpisodeWatches>,
  private val removeEpisodeWatches: Lazy<RemoveEpisodeWatches>,
  private val removeEpisodeWatch: Lazy<RemoveEpisodeWatch>,
  private val logger: Logger,
) : Presenter<EpisodeDetailsUiState> {
  @Composable
  override fun present(): EpisodeDetailsUiState {
    val scope = rememberCoroutineScope()
    val uiMessageManager = remember { UiMessageManager() }

    val refreshing by updateEpisodeDetails.value.inProgress.collectAsState(false)
    val message by uiMessageManager.message.collectAsState(null)

    val showDetails by observeShowDetailsForEpisodeId.value.flow.collectAsRetainedState(null)

    val episodeDetails by observeEpisodeDetails.value.flow.collectAsRetainedState(null)
    val episodeWatches by observeEpisodeWatches.value.flow.collectAsRetainedState(emptyList())

    fun eventSink(event: EpisodeDetailsUiEvent) {
      when (event) {
        is EpisodeDetailsUiEvent.Refresh -> {
          scope.launch {
            updateEpisodeDetails.value.invoke(
              UpdateEpisodeDetails.Params(screen.id, event.fromUser),
            ).onFailure { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        is EpisodeDetailsUiEvent.ClearMessage -> {
          scope.launch {
            uiMessageManager.clearMessage(event.id)
          }
        }

        EpisodeDetailsUiEvent.RemoveAllWatches -> {
          scope.launch {
            removeEpisodeWatches.value.invoke(
              RemoveEpisodeWatches.Params(screen.id),
            ).onFailure { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        is EpisodeDetailsUiEvent.RemoveWatchEntry -> {
          scope.launch {
            removeEpisodeWatch.value.invoke(
              RemoveEpisodeWatch.Params(event.id),
            ).onFailure { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        EpisodeDetailsUiEvent.NavigateUp -> navigator.pop()
        EpisodeDetailsUiEvent.OpenTrackEpisode -> {
          navigator.goTo(EpisodeTrackScreen(screen.id))
        }

        EpisodeDetailsUiEvent.ExpandToShowDetails -> {
          navigator.pop()

          val showId = showDetails?.id ?: return
          // As we pushing a number of different screens onto the back stack,
          // we run it in a single snapshot to avoid unnecessary work
          Snapshot.withMutableSnapshot {
            navigator.goTo(ShowDetailsScreen(showId))
            navigator.goTo(
              ShowSeasonsScreen(
                showId = showId,
                selectedSeasonId = episodeDetails?.season?.id,
                openEpisodeId = episodeDetails?.episode?.id,
              ),
            )
          }
        }
      }
    }

    LaunchedEffect(Unit) {
      observeEpisodeDetails.value.invoke(ObserveEpisodeDetails.Params(screen.id))
      observeEpisodeWatches.value.invoke(ObserveEpisodeWatches.Params(screen.id))
      observeShowDetailsForEpisodeId.value.invoke(ObserveShowDetailsForEpisodeId.Params(screen.id))

      eventSink(EpisodeDetailsUiEvent.Refresh(fromUser = false))
    }

    return EpisodeDetailsUiState(
      episode = episodeDetails?.episode,
      season = episodeDetails?.season,
      watches = episodeWatches,
      canAddEpisodeWatch = episodeDetails?.episode?.hasAired ?: false,
      refreshing = refreshing,
      message = message,
      eventSink = ::eventSink,
    )
  }
}
