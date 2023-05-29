// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
