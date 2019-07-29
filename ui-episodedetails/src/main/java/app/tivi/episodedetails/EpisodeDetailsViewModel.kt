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
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.interactors.AddEpisodeWatch
import app.tivi.interactors.ObserveEpisodeDetails
import app.tivi.interactors.RemoveEpisodeWatch
import app.tivi.interactors.RemoveEpisodeWatches
import app.tivi.interactors.UpdateEpisodeDetails
import app.tivi.interactors.ObserveEpisodeWatches
import app.tivi.interactors.launchInteractor
import app.tivi.episodedetails.EpisodeDetailsViewState.Action
import app.tivi.tmdb.TmdbManager
import app.tivi.TiviMvRxViewModel
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.threeten.bp.OffsetDateTime

class EpisodeDetailsViewModel @AssistedInject constructor(
    @Assisted initialState: EpisodeDetailsViewState,
    private val updateEpisodeDetails: UpdateEpisodeDetails,
    observeEpisodeDetails: ObserveEpisodeDetails,
    private val observeEpisodeWatches: ObserveEpisodeWatches,
    private val addEpisodeWatch: AddEpisodeWatch,
    private val removeEpisodeWatches: RemoveEpisodeWatches,
    private val removeEpisodeWatch: RemoveEpisodeWatch,
    tmdbManager: TmdbManager
) : TiviMvRxViewModel<EpisodeDetailsViewState>(initialState) {
    init {
        observeEpisodeDetails.observe()
                .execute { copy(episode = it) }

        observeEpisodeWatches.observe()
                .execute {
                    val action = if (it is Success && it().isNotEmpty()) Action.UNWATCH else Action.WATCH
                    copy(watches = it, action = action)
                }

        withState {
            observeEpisodeDetails(ObserveEpisodeDetails.Params(it.episodeId))
            observeEpisodeWatches(ObserveEpisodeWatches.Params(it.episodeId))
        }

        tmdbManager.imageProviderObservable
                .execute { copy(tmdbImageUrlProvider = it) }

        refresh()
    }

    private fun refresh() = withState {
        viewModelScope.launchInteractor(updateEpisodeDetails, UpdateEpisodeDetails.Params(it.episodeId, true))
    }

    fun removeWatchEntry(entry: EpisodeWatchEntry) {
        viewModelScope.launchInteractor(removeEpisodeWatch, RemoveEpisodeWatch.Params(entry.id))
    }

    fun markWatched() {
        withState {
            viewModelScope.launchInteractor(addEpisodeWatch, AddEpisodeWatch.Params(it.episodeId, OffsetDateTime.now()))
        }
    }

    fun markUnwatched() {
        withState {
            viewModelScope.launchInteractor(removeEpisodeWatches, RemoveEpisodeWatches.Params(it.episodeId))
        }
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