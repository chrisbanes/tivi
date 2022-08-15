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
import app.tivi.api.UiMessageManager
import app.tivi.domain.interactors.UpdateShowSeasons
import app.tivi.domain.observers.ObserveShowDetails
import app.tivi.domain.observers.ObserveShowSeasonsEpisodesWatches
import app.tivi.extensions.mapToPersistentList
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val logger: Logger
) : ViewModel() {
    private val showId: Long = savedStateHandle["showId"]!!

    private val loadingState = ObservableLoadingCounter()
    private val uiMessageManager = UiMessageManager()

    val state: StateFlow<ShowSeasonsViewState> = combine(
        observeShowSeasons.flow.mapToPersistentList(),
        observeShowDetails.flow,
        loadingState.observable,
        uiMessageManager.message
    ) { seasons, show, refreshing, message ->
        ShowSeasonsViewState(
            show = show,
            seasons = seasons,
            refreshing = refreshing,
            message = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShowSeasonsViewState.Empty
    )

    init {
        observeShowDetails(ObserveShowDetails.Params(showId))
        observeShowSeasons(ObserveShowSeasonsEpisodesWatches.Params(showId))

        refresh(false)
    }

    fun refresh(fromUser: Boolean = true) {
        viewModelScope.launch {
            updateShowSeasons(
                UpdateShowSeasons.Params(showId, fromUser)
            ).collectStatus(loadingState, logger, uiMessageManager)
        }
    }

    fun clearMessage(id: Long) {
        viewModelScope.launch {
            uiMessageManager.clearMessage(id)
        }
    }
}
