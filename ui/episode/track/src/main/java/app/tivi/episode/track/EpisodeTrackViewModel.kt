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
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
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
    private val uiMessageManager = UiMessageManager()

    val state: StateFlow<EpisodeTrackViewState> = combine(
        observeEpisodeDetails.flow,
        loadingState.observable,
        uiMessageManager.message,
    ) { episodeDetails, refreshing, message ->
        EpisodeTrackViewState(
            episode = episodeDetails.episode,
            season = episodeDetails.season,
            refreshing = refreshing,
            message = message,
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

    fun addWatch() {
        viewModelScope.launch {
            addEpisodeWatch(AddEpisodeWatch.Params(episodeId, Clock.System.now()))
                .collectStatus(loadingState, logger, uiMessageManager)
        }
    }

    fun clearMessage(id: Long) {
        viewModelScope.launch {
            uiMessageManager.clearMessage(id)
        }
    }
}
