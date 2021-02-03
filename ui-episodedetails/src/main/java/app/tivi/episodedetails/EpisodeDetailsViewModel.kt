/*
 * Copyright 2018 Google LLC
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

package app.tivi.episodedetails

import androidx.lifecycle.viewModelScope
import app.tivi.ReduxViewModel
import app.tivi.api.UiError
import app.tivi.base.InvokeError
import app.tivi.base.InvokeStarted
import app.tivi.base.InvokeStatus
import app.tivi.base.InvokeSuccess
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.resultentities.EpisodeWithSeason
import app.tivi.domain.interactors.AddEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatches
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeWatches
import app.tivi.ui.SnackbarManager
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime

class EpisodeDetailsViewModel @AssistedInject constructor(
    @Assisted initialState: EpisodeDetailsViewState,
    private val updateEpisodeDetails: UpdateEpisodeDetails,
    observeEpisodeDetails: ObserveEpisodeDetails,
    private val observeEpisodeWatches: ObserveEpisodeWatches,
    private val addEpisodeWatch: AddEpisodeWatch,
    private val removeEpisodeWatches: RemoveEpisodeWatches,
    private val removeEpisodeWatch: RemoveEpisodeWatch,
    private val logger: Logger,
    private val snackbarManager: SnackbarManager
) : ReduxViewModel<EpisodeDetailsViewState>(initialState) {

    private val loadingState = ObservableLoadingCounter()

    private val pendingActions = MutableSharedFlow<EpisodeDetailsAction>()

    init {
        viewModelScope.launch {
            observeEpisodeDetails.observe()
                .collect { updateFromEpisodeDetails(it) }
        }

        viewModelScope.launch {
            observeEpisodeWatches.observe()
                .onStart { emit(emptyList()) }
                .collect { updateFromEpisodeWatches(it) }
        }

        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    RefreshAction -> refresh(true)
                    AddEpisodeWatchAction -> markWatched()
                    RemoveAllEpisodeWatchesAction -> markUnwatched()
                    is RemoveEpisodeWatchAction -> removeWatchEntry(action)
                    ClearError -> snackbarManager.removeCurrentError()
                }
            }
        }

        snackbarManager.launchInScope(viewModelScope) { uiError, visible ->
            viewModelScope.launchSetState {
                copy(error = if (visible) uiError else null)
            }
        }

        viewModelScope.launch {
            loadingState.observable.collectAndSetState { copy(refreshing = it) }
        }

        selectSubscribe(EpisodeDetailsViewState::episodeId) { episodeId ->
            observeEpisodeDetails(ObserveEpisodeDetails.Params(episodeId))
            observeEpisodeWatches(ObserveEpisodeWatches.Params(episodeId))

            refresh(false)
        }
    }

    private fun updateFromEpisodeDetails(episodeWithSeason: EpisodeWithSeason) {
        viewModelScope.launchSetState {
            val firstAired = episodeWithSeason.episode?.firstAired
            copy(
                episode = episodeWithSeason.episode,
                season = episodeWithSeason.season,
                canAddEpisodeWatch = firstAired?.isBefore(OffsetDateTime.now()) == true
            )
        }
    }

    private fun updateFromEpisodeWatches(watches: List<EpisodeWatchEntry>) {
        viewModelScope.launchSetState {
            copy(watches = watches)
        }
    }

    internal fun submitAction(action: EpisodeDetailsAction) {
        viewModelScope.launch {
            pendingActions.emit(action)
        }
    }

    private fun refresh(fromUserInteraction: Boolean) = viewModelScope.withState { state ->
        updateEpisodeDetails(
            UpdateEpisodeDetails.Params(state.episodeId, fromUserInteraction)
        ).watchStatus()
    }

    private fun removeWatchEntry(action: RemoveEpisodeWatchAction) {
        removeEpisodeWatch(RemoveEpisodeWatch.Params(action.watchId)).watchStatus()
    }

    private fun markWatched() = viewModelScope.withState { state ->
        addEpisodeWatch(AddEpisodeWatch.Params(state.episodeId, OffsetDateTime.now())).watchStatus()
    }

    private fun markUnwatched() = viewModelScope.withState { state ->
        removeEpisodeWatches(RemoveEpisodeWatches.Params(state.episodeId)).watchStatus()
    }

    private fun Flow<InvokeStatus>.watchStatus() = viewModelScope.launch { collectStatus() }

    private suspend fun Flow<InvokeStatus>.collectStatus() = collect { status ->
        when (status) {
            InvokeStarted -> loadingState.addLoader()
            InvokeSuccess -> loadingState.removeLoader()
            is InvokeError -> {
                logger.i(status.throwable)
                snackbarManager.sendError(UiError(status.throwable))
                loadingState.removeLoader()
            }
        }
    }

    @AssistedFactory
    internal interface Factory {
        fun create(initialState: EpisodeDetailsViewState): EpisodeDetailsViewModel
    }
}

internal fun EpisodeDetailsViewModel.Factory.create(
    episodeId: Long
): EpisodeDetailsViewModel {
    return create(EpisodeDetailsViewState(episodeId = episodeId))
}
