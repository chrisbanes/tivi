// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.showdetails.seasons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.tivi.common.compose.UiMessage
import app.tivi.common.compose.UiMessageManager
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.data.models.TiviShow
import app.tivi.domain.interactors.UpdateShowSeasons
import app.tivi.domain.observers.ObserveShowDetails
import app.tivi.domain.observers.ObserveShowSeasonsEpisodesWatches
import app.tivi.screens.ShowSeasonsScreen
import app.tivi.util.Logger
import app.tivi.util.onException
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ShowSeasonsUiPresenterFactory(
  private val presenterFactory: (ShowSeasonsScreen, Navigator) -> ShowSeasonsPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is ShowSeasonsScreen -> presenterFactory(screen, navigator)
    else -> null
  }
}

@Inject
class ShowSeasonsPresenter(
  @Assisted private val screen: ShowSeasonsScreen,
  @Assisted private val navigator: Navigator,
  private val observeShowDetails: Lazy<ObserveShowDetails>,
  private val observeShowSeasons: Lazy<ObserveShowSeasonsEpisodesWatches>,
  private val updateShowSeasons: Lazy<UpdateShowSeasons>,
  private val logger: Logger,
) : Presenter<ShowSeasonsUiState> {
  @Composable
  override fun present(): ShowSeasonsUiState {
    val scope = rememberCoroutineScope()

    val uiMessageManager = remember { UiMessageManager() }

    val seasons by observeShowSeasons.value.flow.collectAsRetainedState(emptyList())
    val show by observeShowDetails.value.flow.collectAsRetainedState(TiviShow.EMPTY_SHOW)

    var openedEpisodeId by rememberRetained { mutableStateOf(screen.openEpisodeId) }

    val refreshing by updateShowSeasons.value.inProgress.collectAsState(false)
    val message by uiMessageManager.message.collectAsState(null)

    fun eventSink(event: ShowSeasonsUiEvent) {
      when (event) {
        is ShowSeasonsUiEvent.ClearMessage -> {
          scope.launch {
            uiMessageManager.clearMessage(event.id)
          }
        }

        is ShowSeasonsUiEvent.OpenEpisodeDetails -> {
          openedEpisodeId = event.id
        }

        is ShowSeasonsUiEvent.Refresh -> {
          scope.launch {
            updateShowSeasons.value.invoke(
              UpdateShowSeasons.Params(screen.id, event.fromUser),
            ).onException { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        ShowSeasonsUiEvent.NavigateBack -> navigator.pop()
      }
    }

    LaunchedEffect(Unit) {
      observeShowDetails.value.invoke(ObserveShowDetails.Params(screen.id))
      observeShowSeasons.value.invoke(ObserveShowSeasonsEpisodesWatches.Params(screen.id))

      eventSink(ShowSeasonsUiEvent.Refresh(false))
    }

    return ShowSeasonsUiState(
      show = show,
      seasons = seasons,
      refreshing = refreshing,
      openedEpisodeId = openedEpisodeId,
      message = message,
      initialSeasonId = screen.selectedSeasonId,
      eventSink = ::eventSink,
    )
  }
}
