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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tivi.api.UiMessageManager
import app.tivi.domain.interactors.AddEpisodeWatch
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.extensions.combine
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
    observeEpisodeDetails: ObserveEpisodeDetails,
    private val addEpisodeWatch: AddEpisodeWatch,
    private val logger: Logger,
) : ViewModel() {
    private val episodeId: Long = savedStateHandle["episodeId"]!!

    private val loadingState = ObservableLoadingCounter()
    private val submittingState = ObservableLoadingCounter()
    private val uiMessageManager = UiMessageManager()

    private val selectedDate = MutableStateFlow<LocalDate?>(null)
    private val selectedTime = MutableStateFlow<LocalTime?>(null)
    private val selectedNow = MutableStateFlow(true)

    val state: StateFlow<EpisodeTrackViewState> = combine(
        observeEpisodeDetails.flow,
        selectedDate,
        selectedTime,
        selectedNow,
        loadingState.observable,
        submittingState.observable,
        uiMessageManager.message,
    ) { episodeDetails, selectedDate, selectedTime, selectedNow, refreshing, submitting, message ->
        EpisodeTrackViewState(
            episode = episodeDetails.episode,
            season = episodeDetails.season,
            showSetFirstAired = episodeDetails.episode?.firstAired != null,
            selectedDate = selectedDate,
            selectedTime = selectedTime,
            selectedNow = selectedNow,
            refreshing = refreshing,
            message = message,
            submitInProgress = submitting,
            canSubmit = !submitting && (selectedNow || (selectedDate != null && selectedTime != null)),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = EpisodeTrackViewState.Empty,
    )

    init {
        observeEpisodeDetails(ObserveEpisodeDetails.Params(episodeId))

        refresh(false)
    }

    fun refresh(fromUserInteraction: Boolean = true) {
        viewModelScope.launch {
            updateEpisodeDetails(
                UpdateEpisodeDetails.Params(episodeId, fromUserInteraction),
            ).collectStatus(loadingState, logger, uiMessageManager)
        }
    }

    private val selectedDateTime: LocalDateTime?
        get() {
            val date = selectedDate.value
            val time = selectedTime.value
            return if (date != null && time != null) LocalDateTime(date, time) else null
        }

    fun submitWatch() {
        val dt = selectedDateTime
        val instant = when {
            selectedNow.value -> Clock.System.now()
            dt != null -> dt.toInstant(TimeZone.currentSystemDefault())
            else -> null
        }

        if (instant != null) {
            viewModelScope.launch {
                addEpisodeWatch(AddEpisodeWatch.Params(episodeId, instant))
                    .collectStatus(submittingState, logger, uiMessageManager)
            }
        } else {
            // TODO: display error message
        }
    }

    fun selectNow(selected: Boolean) {
        selectedNow.tryEmit(selected)
    }

    fun selectEpisodeFirstAired() {
        val episodeFirstAired = state.value.episode?.firstAired
        if (episodeFirstAired != null) {
            val dt = episodeFirstAired.toLocalDateTime(TimeZone.currentSystemDefault())
            selectedDate.tryEmit(dt.date)
            selectedTime.tryEmit(dt.time)
        } else {
            // TODO: display error message
        }
    }

    fun selectDate(date: LocalDate) {
        selectedDate.tryEmit(date)
    }

    fun selectTime(time: LocalTime) {
        selectedTime.tryEmit(time)
    }

    fun clearMessage(id: Long) {
        viewModelScope.launch {
            uiMessageManager.clearMessage(id)
        }
    }
}
