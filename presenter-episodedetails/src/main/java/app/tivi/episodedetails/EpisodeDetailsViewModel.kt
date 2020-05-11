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

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.tivi.TiviMvRxViewModel
import app.tivi.api.UiError
import app.tivi.base.InvokeError
import app.tivi.base.InvokeIdle
import app.tivi.base.InvokeStarted
import app.tivi.base.InvokeStatus
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.resultentities.EpisodeWithSeason
import app.tivi.domain.interactors.AddEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatches
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeWatches
import app.tivi.ui.SnackbarManager
import app.tivi.util.ObservableLoadingCounter
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
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
    private val removeEpisodeWatch: RemoveEpisodeWatch
) : TiviMvRxViewModel<EpisodeDetailsViewState>(initialState) {

    private val loadingState = ObservableLoadingCounter()
    private val snackbarManager = SnackbarManager()

    private val pendingActions = Channel<EpisodeDetailsAction>(Channel.BUFFERED)

    init {
        viewModelScope.launchObserve(observeEpisodeDetails) {
            it.collect { result -> updateFromEpisodeDetails(result) }
        }

        viewModelScope.launchObserve(observeEpisodeWatches) {
            it.onStart {
                emit(emptyList())
            }.collect { result -> updateFromEpisodeWatches(result) }
        }

        viewModelScope.launch {
            for (action in pendingActions) when (action) {
                RefreshAction -> refresh(true)
                AddEpisodeWatchAction -> markWatched()
                RemoveAllEpisodeWatchesAction -> markUnwatched()
                is RemoveEpisodeWatchAction -> removeWatchEntry(action)
                ClearError -> snackbarManager.removeCurrentError()
            }
        }

        viewModelScope.launch {
            snackbarManager.launch { uiError, visible ->
                setState {
                    copy(error = if (visible) uiError else null)
                }
            }
        }

        viewModelScope.launch {
            loadingState.observable.collect {
                setState { copy(refreshing = it) }
            }
        }

        withState {
            observeEpisodeDetails(ObserveEpisodeDetails.Params(it.episodeId))
            observeEpisodeWatches(ObserveEpisodeWatches.Params(it.episodeId))
        }

        refresh(false)
    }

    private fun updateFromEpisodeDetails(episodeWithSeason: EpisodeWithSeason) = setState {
        val firstAired = episodeWithSeason.episode?.firstAired
        copy(
            episode = episodeWithSeason.episode,
            season = episodeWithSeason.season,
            canAddEpisodeWatch = firstAired?.isBefore(OffsetDateTime.now()) == true
        )
    }

    private fun updateFromEpisodeWatches(watches: List<EpisodeWatchEntry>) = setState {
        copy(watches = watches)
    }

    fun submitAction(action: EpisodeDetailsAction) {
        viewModelScope.launch { pendingActions.send(action) }
    }

    private fun refresh(fromUserInteraction: Boolean) = withState {
        updateEpisodeDetails(
            UpdateEpisodeDetails.Params(it.episodeId, fromUserInteraction)
        ).watchStatus()
    }

    private fun removeWatchEntry(action: RemoveEpisodeWatchAction) {
        removeEpisodeWatch(RemoveEpisodeWatch.Params(action.watchId)).watchStatus()
    }

    private fun markWatched() = withState {
        addEpisodeWatch(AddEpisodeWatch.Params(it.episodeId, OffsetDateTime.now())).watchStatus()
    }

    private fun markUnwatched() = withState {
        removeEpisodeWatches(RemoveEpisodeWatches.Params(it.episodeId)).watchStatus()
    }

    private fun Flow<InvokeStatus>.watchStatus() = viewModelScope.launch { collectStatus() }

    private suspend fun Flow<InvokeStatus>.collectStatus() = collect { status ->
        when (status) {
            is InvokeIdle -> Unit
            is InvokeStarted -> loadingState.addLoader()
            is InvokeError -> {
                snackbarManager.sendError(UiError(status.throwable))
                loadingState.removeLoader()
            }
            else -> loadingState.removeLoader()
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: EpisodeDetailsViewState): EpisodeDetailsViewModel
    }

    interface FactoryProvider {
        fun provideFactory(): Factory
    }

    companion object : MvRxViewModelFactory<EpisodeDetailsViewModel, EpisodeDetailsViewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: EpisodeDetailsViewState
        ): EpisodeDetailsViewModel? {
            val fvmc = viewModelContext as FragmentViewModelContext
            val f: FactoryProvider = (fvmc.fragment<Fragment>()) as FactoryProvider
            return f.provideFactory().create(state)
        }

        override fun initialState(
            viewModelContext: ViewModelContext
        ): EpisodeDetailsViewState? {
            val f: Fragment = (viewModelContext as FragmentViewModelContext).fragment()
            val args = f.requireArguments()
            return EpisodeDetailsViewState(episodeId = args.getLong("episode_id"))
        }
    }
}
