/*
 * Copyright 2021 Google LLC
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

package app.tivi.showdetails.seasons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tivi.api.UiMessage
import app.tivi.api.UiMessageManager
import app.tivi.base.InvokeError
import app.tivi.base.InvokeStarted
import app.tivi.base.InvokeStatus
import app.tivi.base.InvokeSuccess
import app.tivi.domain.interactors.UpdateShowSeasons
import app.tivi.domain.observers.ObserveShowDetails
import app.tivi.domain.observers.ObserveShowSeasonsEpisodesWatches
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShowSeasonsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeShowDetails: ObserveShowDetails,
    observeShowSeasons: ObserveShowSeasonsEpisodesWatches,
    private val updateShowSeasons: UpdateShowSeasons,
    private val logger: Logger,
) : ViewModel() {
    private val showId: Long = savedStateHandle.get("showId")!!

    private val loadingState = ObservableLoadingCounter()
    private val uiMessageManager = UiMessageManager()

    val state: StateFlow<ShowSeasonsViewState> = combine(
        observeShowSeasons.flow,
        observeShowDetails.flow,
        loadingState.observable,
        uiMessageManager.messages,
    ) { seasons, show, refreshing, messages ->
        ShowSeasonsViewState(
            show = show,
            seasons = seasons,
            refreshing = refreshing,
            messages = messages,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShowSeasonsViewState.Empty,
    )

    init {
        observeShowDetails(ObserveShowDetails.Params(showId))
        observeShowSeasons(ObserveShowSeasonsEpisodesWatches.Params(showId))

        refresh(false)
    }

    fun refresh(fromUser: Boolean = true) {
        viewModelScope.launch {
            updateShowSeasons(UpdateShowSeasons.Params(showId, fromUser)).collectStatus()
        }
    }

    private suspend fun Flow<InvokeStatus>.collectStatus() = collect { status ->
        when (status) {
            InvokeStarted -> loadingState.addLoader()
            InvokeSuccess -> loadingState.removeLoader()
            is InvokeError -> {
                logger.i(status.throwable)
                uiMessageManager.emitMessage(UiMessage(status.throwable))
                loadingState.removeLoader()
            }
        }
    }

    fun clearMessage(id: Long) {
        viewModelScope.launch {
            uiMessageManager.clearMessage(id)
        }
    }
}
