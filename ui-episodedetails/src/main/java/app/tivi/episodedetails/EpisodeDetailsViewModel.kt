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
import app.tivi.TiviMvRxViewModel
import app.tivi.domain.interactors.AddEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatches
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeWatches
import app.tivi.episodedetails.EpisodeDetailsViewState.Action
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
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

    private val pendingActions = Channel<EpisodeDetailsAction>(Channel.BUFFERED)

    init {
        viewModelScope.launchObserve(observeEpisodeDetails) {
            it.execute { result ->
                val episode = result()?.episode
                val season = result()?.season
                copy(episode = episode, season = season)
            }
        }

        viewModelScope.launchObserve(observeEpisodeWatches) {
            it.onStart {
                emit(emptyList())
            }.execute { result ->
                val action = if (result is Success && result().isNotEmpty()) Action.UNWATCH else Action.WATCH
                copy(watches = result() ?: emptyList(), action = action)
            }
        }

        viewModelScope.launch {
            for (action in pendingActions) {
                when (action) {
                    RefreshAction -> refresh()
                    AddEpisodeWatchAction -> markWatched()
                    RemoveAllEpisodeWatchesAction -> markUnwatched()
                    is RemoveEpisodeWatchAction -> removeWatchEntry(action)
                }
            }
        }

        withState {
            observeEpisodeDetails(ObserveEpisodeDetails.Params(it.episodeId))
            observeEpisodeWatches(ObserveEpisodeWatches.Params(it.episodeId))
        }

        refresh()
    }

    fun submitAction(action: EpisodeDetailsAction) {
        viewModelScope.launch { pendingActions.send(action) }
    }

    private fun refresh() = withState {
        updateEpisodeDetails(UpdateEpisodeDetails.Params(it.episodeId, true))
    }

    private fun removeWatchEntry(action: RemoveEpisodeWatchAction) {
        removeEpisodeWatch(RemoveEpisodeWatch.Params(action.watchId))
    }

    private fun markWatched() = withState {
        addEpisodeWatch(AddEpisodeWatch.Params(it.episodeId, OffsetDateTime.now()))
    }

    private fun markUnwatched() = withState {
        removeEpisodeWatches(RemoveEpisodeWatches.Params(it.episodeId))
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: EpisodeDetailsViewState): EpisodeDetailsViewModel
    }

    companion object : MvRxViewModelFactory<EpisodeDetailsViewModel, EpisodeDetailsViewState> {
        override fun create(viewModelContext: ViewModelContext, state: EpisodeDetailsViewState): EpisodeDetailsViewModel? {
            val fragment: EpisodeDetailsFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.episodeDetailsViewModelFactory.create(state)
        }
    }
}