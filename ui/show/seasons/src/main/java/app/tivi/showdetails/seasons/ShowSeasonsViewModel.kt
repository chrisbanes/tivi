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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tivi.api.UiMessageManager
import app.tivi.data.models.TiviShow
import app.tivi.domain.interactors.UpdateShowSeasons
import app.tivi.domain.observers.ObserveShowDetails
import app.tivi.domain.observers.ObserveShowSeasonsEpisodesWatches
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ShowSeasonsViewModel(
    @Assisted savedStateHandle: SavedStateHandle,
    private val observeShowDetails: ObserveShowDetails,
    private val observeShowSeasons: ObserveShowSeasonsEpisodesWatches,
    private val updateShowSeasons: UpdateShowSeasons,
    private val logger: Logger,
) : ViewModel() {
    private val showId: Long = savedStateHandle["showId"]!!

    @Composable
    fun presenter(): ShowSeasonsViewState {
        val scope = rememberCoroutineScope()

        val loadingState = remember { ObservableLoadingCounter() }
        val uiMessageManager = remember { UiMessageManager() }

        val seasons by observeShowSeasons.flow.collectAsState(emptyList())
        val show by observeShowDetails.flow.collectAsState(TiviShow.EMPTY_SHOW)
        val refreshing by loadingState.observable.collectAsState(false)
        val message by uiMessageManager.message.collectAsState(null)

        fun eventSink(event: ShowSeasonsUiEvent) {
            when (event) {
                is ShowSeasonsUiEvent.ClearMessage -> {
                    viewModelScope.launch {
                        uiMessageManager.clearMessage(event.id)
                    }
                }

                is ShowSeasonsUiEvent.Refresh -> {
                    scope.launch {
                        updateShowSeasons(
                            UpdateShowSeasons.Params(showId, event.fromUser),
                        ).collectStatus(loadingState, logger, uiMessageManager)
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            observeShowDetails(ObserveShowDetails.Params(showId))
            observeShowSeasons(ObserveShowSeasonsEpisodesWatches.Params(showId))

            eventSink(ShowSeasonsUiEvent.Refresh(false))
        }

        return ShowSeasonsViewState(
            show = show,
            seasons = seasons,
            refreshing = refreshing,
            message = message,
            eventSink = ::eventSink,
        )
    }
}
