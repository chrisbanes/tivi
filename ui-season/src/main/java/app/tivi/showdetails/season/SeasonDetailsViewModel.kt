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

package app.tivi.showdetails.season

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tivi.api.UiError
import app.tivi.base.InvokeError
import app.tivi.base.InvokeStarted
import app.tivi.base.InvokeStatus
import app.tivi.base.InvokeSuccess
import app.tivi.data.entities.ActionDate
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus.Action
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus.Params
import app.tivi.domain.observers.ObserveShowSeason
import app.tivi.ui.SnackbarManager
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("unused")
@HiltViewModel
internal class SeasonDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeShowSeason: ObserveShowSeason,
    private val changeSeasonWatchedStatus: ChangeSeasonWatchedStatus,
    private val logger: Logger,
    private val snackbarManager: SnackbarManager
) : ViewModel() {
    private val seasonId: Long = savedStateHandle.get("seasonId")!!

    private val loadingState = ObservableLoadingCounter()

    val state = combine(
        observeShowSeason.observe().distinctUntilChanged(),
        loadingState.observable,
        snackbarManager.errors,
    ) { seasonWithEpisodes, refreshing, error ->
        SeasonDetailsViewState(
            season = seasonWithEpisodes.season,
            episodes = seasonWithEpisodes.episodes,
            refreshing = refreshing,
            refreshError = error,
        )
    }

    init {
        observeShowSeason(ObserveShowSeason.Params(seasonId))

        refresh(false)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun refresh(fromUser: Boolean) {
        // TODO
    }

    private fun Flow<InvokeStatus>.watchStatus() = viewModelScope.launch { collectStatus() }

    private suspend fun Flow<InvokeStatus>.collectStatus() = collect { status ->
        when (status) {
            InvokeStarted -> loadingState.addLoader()
            InvokeSuccess -> loadingState.removeLoader()
            is InvokeError -> {
                logger.i(status.throwable)
                snackbarManager.addError(UiError(status.throwable))
                loadingState.removeLoader()
            }
        }
    }

    fun markSeasonWatched(
        onlyAired: Boolean = false,
        date: ActionDate = ActionDate.NOW,
    ) {
        changeSeasonWatchedStatus(
            Params(seasonId, Action.WATCHED, onlyAired, date)
        ).watchStatus()
    }

    fun markSeasonUnwatched() {
        changeSeasonWatchedStatus(Params(seasonId, Action.UNWATCH)).watchStatus()
    }

    fun clearError() {
        viewModelScope.launch {
            snackbarManager.removeCurrentError()
        }
    }
}
