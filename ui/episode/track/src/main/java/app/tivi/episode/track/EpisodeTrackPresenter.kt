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
import androidx.compose.runtime.setValue
import app.tivi.api.UiMessage
import app.tivi.api.UiMessageManager
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.domain.interactors.AddEpisodeWatch
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.screens.EpisodeTrackScreen
import app.tivi.util.Logger
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
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
    private val updateEpisodeDetails: UpdateEpisodeDetails,
    private val observeEpisodeDetails: ObserveEpisodeDetails,
    private val addEpisodeWatch: AddEpisodeWatch,
    private val logger: Logger,
) : Presenter<EpisodeTrackUiState> {
    @Composable
    override fun present(): EpisodeTrackUiState {
        val scope = rememberCoroutineScope()
        val uiMessageManager = remember { UiMessageManager() }

        var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
        var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
        var selectedNow by remember { mutableStateOf(false) }

        val episodeDetails by observeEpisodeDetails.flow.collectAsState(initial = null)

        val refreshing by updateEpisodeDetails.inProgress.collectAsState(initial = false)
        val submitting by addEpisodeWatch.inProgress.collectAsState(initial = false)
        val message by uiMessageManager.message.collectAsState(initial = null)

        var dismissed by remember { mutableStateOf(false) }

        val selectedDateTime by remember {
            derivedStateOf {
                val date = selectedDate
                val time = selectedTime
                if (date != null && time != null) LocalDateTime(date, time) else null
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
                        updateEpisodeDetails(
                            UpdateEpisodeDetails.Params(screen.id, event.fromUser),
                        ).also { result ->
                            result.exceptionOrNull()?.let { e ->
                                logger.i(e)
                                uiMessageManager.emitMessage(UiMessage(e))
                            }
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
                    selectedNow = true
                }

                EpisodeTrackUiEvent.UnselectNow -> {
                    selectedNow = false
                }

                is EpisodeTrackUiEvent.SelectTime -> {
                    selectedTime = event.time
                }

                EpisodeTrackUiEvent.Submit -> {
                    val dt = selectedDateTime
                    val instant = when {
                        selectedNow -> Clock.System.now()
                        dt != null -> dt.toInstant(TimeZone.currentSystemDefault())
                        else -> null
                    }

                    if (instant != null) {
                        scope.launch {
                            addEpisodeWatch(
                                AddEpisodeWatch.Params(screen.id, instant),
                            ).also { result ->
                                if (result.isSuccess) {
                                    dismissed = true
                                }
                                result.exceptionOrNull()?.let { e ->
                                    logger.i(e)
                                    uiMessageManager.emitMessage(UiMessage(e))
                                }
                            }
                        }
                    } else {
                        // TODO: display error message
                    }
                }

                EpisodeTrackUiEvent.NavigateUp -> navigator.pop()
            }
        }

        LaunchedEffect(Unit) {
            observeEpisodeDetails(ObserveEpisodeDetails.Params(screen.id))
            eventSink(EpisodeTrackUiEvent.Refresh(false))
        }

        return EpisodeTrackUiState(
            episode = episodeDetails?.episode,
            season = episodeDetails?.season,
            showSetFirstAired = episodeDetails?.episode?.firstAired != null,
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            selectedNow = selectedNow,
            refreshing = refreshing,
            message = message,
            submitInProgress = submitting,
            shouldDismiss = dismissed,
            canSubmit = !submitting && (selectedNow || (selectedDate != null && selectedTime != null)),
            eventSink = ::eventSink,
        )
    }
}
