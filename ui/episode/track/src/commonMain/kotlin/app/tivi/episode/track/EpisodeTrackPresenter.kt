// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.episode.track

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.tivi.common.compose.UiMessage
import app.tivi.common.compose.UiMessageManager
import app.tivi.domain.interactors.AddEpisodeWatch
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.screens.EpisodeTrackScreen
import app.tivi.util.Logger
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class EpisodeTrackUiPresenterFactory(
  private val presenterFactory: (EpisodeTrackScreen, Navigator) -> EpisodeTrackPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is EpisodeTrackScreen -> presenterFactory(screen, navigator)
    else -> null
  }
}

@Inject
class EpisodeTrackPresenter(
  @Assisted private val screen: EpisodeTrackScreen,
  @Assisted private val navigator: Navigator,
  private val updateEpisodeDetails: Lazy<UpdateEpisodeDetails>,
  private val observeEpisodeDetails: Lazy<ObserveEpisodeDetails>,
  private val addEpisodeWatch: Lazy<AddEpisodeWatch>,
  private val logger: Logger,
) : Presenter<EpisodeTrackUiState> {
  @Composable
  override fun present(): EpisodeTrackUiState {
    val scope = rememberCoroutineScope()
    val uiMessageManager = remember { UiMessageManager() }

    val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    var selectedDate by remember { mutableStateOf(now.date) }
    var selectedTime by remember { mutableStateOf(now.time) }

    val episodeDetails by observeEpisodeDetails.value.flow.collectAsRetainedState(initial = null)

    val refreshing by updateEpisodeDetails.value.inProgress.collectAsState(initial = false)
    val submitting by addEpisodeWatch.value.inProgress.collectAsState(initial = false)
    val message by uiMessageManager.message.collectAsState(initial = null)

    val selectedDateTime by remember {
      derivedStateOf {
        LocalDateTime(selectedDate, selectedTime)
      }
    }

    fun eventSink(event: EpisodeTrackUiEvent) {
      when (event) {
        is EpisodeTrackUiEvent.ClearMessage -> {
          scope.launch {
            uiMessageManager.clearMessage(event.id)
          }
        }

        is EpisodeTrackUiEvent.Refresh -> {
          scope.launch {
            updateEpisodeDetails.value.invoke(
              UpdateEpisodeDetails.Params(screen.id, event.fromUser),
            ).onFailure { e ->
              logger.i(e)
              uiMessageManager.emitMessage(UiMessage(e))
            }
          }
        }

        is EpisodeTrackUiEvent.SelectDate -> {
          selectedDate = event.date
        }

        EpisodeTrackUiEvent.SelectFirstAired -> {
          episodeDetails?.episode?.firstAired
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.also { dateTime ->
              selectedDate = dateTime.date
              selectedTime = dateTime.time
            }
        }

        EpisodeTrackUiEvent.SelectNow -> {
          Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            .also { dateTime ->
              selectedDate = dateTime.date
              selectedTime = dateTime.time
            }
        }

        is EpisodeTrackUiEvent.SelectTime -> {
          selectedTime = event.time
        }

        EpisodeTrackUiEvent.Submit -> {
          scope.launch {
            addEpisodeWatch.value.invoke(
              AddEpisodeWatch.Params(
                episodeId = screen.id,
                timestamp = selectedDateTime.toInstant(TimeZone.currentSystemDefault()),
              ),
            ).also { result ->
              if (result.isSuccess) {
                navigator.pop()
              }
              result.onFailure { e ->
                logger.i(e)
                uiMessageManager.emitMessage(UiMessage(e))
              }
            }
          }
        }
      }
    }

    LaunchedEffect(Unit) {
      observeEpisodeDetails.value.invoke(ObserveEpisodeDetails.Params(screen.id))
      eventSink(EpisodeTrackUiEvent.Refresh(false))
    }

    return EpisodeTrackUiState(
      episode = episodeDetails?.episode,
      season = episodeDetails?.season,
      showSetFirstAired = episodeDetails?.episode?.firstAired != null,
      selectedDate = selectedDate,
      selectedTime = selectedTime,
      refreshing = refreshing,
      message = message,
      submitInProgress = submitting,
      canSubmit = !submitting,
      eventSink = ::eventSink,
    )
  }
}
