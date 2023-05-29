// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.episodedetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import app.tivi.api.UiMessageManager
import app.tivi.domain.interactors.RemoveEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatches
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeWatches
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class EpisodeDetailsViewModel(
    @Assisted savedStateHandle: SavedStateHandle,
    private val updateEpisodeDetails: UpdateEpisodeDetails,
    private val observeEpisodeDetails: ObserveEpisodeDetails,
    private val observeEpisodeWatches: ObserveEpisodeWatches,
    private val removeEpisodeWatches: RemoveEpisodeWatches,
    private val removeEpisodeWatch: RemoveEpisodeWatch,
    private val logger: Logger,
) : ViewModel() {
    private val episodeId: Long = savedStateHandle["episodeId"]!!

    @Composable
    fun presenter(): EpisodeDetailsViewState {
        val scope = rememberCoroutineScope()

        val loadingState = remember { ObservableLoadingCounter() }
        val uiMessageManager = remember { UiMessageManager() }

        val refreshing by loadingState.observable.collectAsState(false)
        val message by uiMessageManager.message.collectAsState(null)

        val episodeDetails by observeEpisodeDetails.flow.collectAsState(null)
        val episodeWatches by observeEpisodeWatches.flow.collectAsState(emptyList())

        fun eventSink(event: EpisodeDetailsUiEvent) = when (event) {
            is EpisodeDetailsUiEvent.Refresh -> {
                scope.launch {
                    updateEpisodeDetails(
                        UpdateEpisodeDetails.Params(episodeId, event.fromUser),
                    ).collectStatus(loadingState, logger, uiMessageManager)
                }
            }

            is EpisodeDetailsUiEvent.ClearMessage -> {
                scope.launch {
                    uiMessageManager.clearMessage(event.id)
                }
            }

            EpisodeDetailsUiEvent.RemoveAllWatches -> {
                scope.launch {
                    removeEpisodeWatches(
                        RemoveEpisodeWatches.Params(episodeId),
                    ).collectStatus(loadingState, logger, uiMessageManager)
                }
            }

            is EpisodeDetailsUiEvent.RemoveWatchEntry -> {
                scope.launch {
                    removeEpisodeWatch(
                        RemoveEpisodeWatch.Params(event.id),
                    ).collectStatus(loadingState, logger, uiMessageManager)
                }
            }
        }

        LaunchedEffect(Unit) {
            observeEpisodeDetails(ObserveEpisodeDetails.Params(episodeId))
            observeEpisodeWatches(ObserveEpisodeWatches.Params(episodeId))

            eventSink(EpisodeDetailsUiEvent.Refresh(fromUser = false))
        }

        return EpisodeDetailsViewState(
            episode = episodeDetails?.episode,
            season = episodeDetails?.season,
            watches = episodeWatches,
            canAddEpisodeWatch = episodeDetails?.episode?.hasAired ?: false,
            refreshing = refreshing,
            message = message,
            eventSink = ::eventSink,
        )
    }
}
