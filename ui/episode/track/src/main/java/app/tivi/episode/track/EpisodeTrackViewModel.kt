/*
 * Copyright 2023 Google LLC
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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import app.tivi.api.UiMessageManager
import app.tivi.base.InvokeSuccess
import app.tivi.domain.interactors.AddEpisodeWatch
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import app.tivi.util.onEachStatus
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
class EpisodeTrackViewModel(
    @Assisted savedStateHandle: SavedStateHandle,
    private val updateEpisodeDetails: UpdateEpisodeDetails,
    private val observeEpisodeDetails: ObserveEpisodeDetails,
    private val addEpisodeWatch: AddEpisodeWatch,
    private val logger: Logger,
) : ViewModel() {
    private val episodeId: Long = savedStateHandle["episodeId"]!!

    @Composable
    fun presenter(): EpisodeTrackViewState {
        val scope = rememberCoroutineScope()

        val loadingState = remember { ObservableLoadingCounter() }
        val submittingState = remember { ObservableLoadingCounter() }
        val uiMessageManager = remember { UiMessageManager() }

        var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
        var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
        var selectedNow by remember { mutableStateOf(false) }

        val episodeDetails by observeEpisodeDetails.flow.collectAsState(initial = null)

        val refreshing by loadingState.observable.collectAsState(initial = false)
        val submitting by submittingState.observable.collectAsState(initial = false)
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
                            UpdateEpisodeDetails.Params(episodeId, event.fromUser),
                        ).collectStatus(loadingState, logger, uiMessageManager)
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
                            addEpisodeWatch(AddEpisodeWatch.Params(episodeId, instant))
                                .onEachStatus(submittingState, logger, uiMessageManager)
                                .collect { status ->
                                    if (status == InvokeSuccess) {
                                        dismissed = true
                                    }
                                }
                        }
                    } else {
                        // TODO: display error message
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            observeEpisodeDetails(ObserveEpisodeDetails.Params(episodeId))
            eventSink(EpisodeTrackUiEvent.Refresh(false))
        }

        return EpisodeTrackViewState(
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
