/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.showdetails.episodedetails

import android.support.v4.app.FragmentActivity
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.interactors.AddEpisodeWatch
import app.tivi.interactors.RemoveEpisodeWatch
import app.tivi.interactors.RemoveEpisodeWatches
import app.tivi.interactors.UpdateEpisodeDetails
import app.tivi.interactors.UpdateEpisodeWatches
import app.tivi.interactors.launchInteractor
import app.tivi.showdetails.ShowDetailsActivity
import app.tivi.showdetails.episodedetails.EpisodeDetailsViewState.Action
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppRxSchedulers
import app.tivi.util.TiviMvRxViewModel
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.threeten.bp.OffsetDateTime
import java.util.concurrent.TimeUnit

class EpisodeDetailsViewModel @AssistedInject constructor(
    @Assisted initialState: EpisodeDetailsViewState,
    schedulers: AppRxSchedulers,
    private val updateEpisodeDetails: UpdateEpisodeDetails,
    private val updateEpisodeWatches: UpdateEpisodeWatches,
    private val addEpisodeWatch: AddEpisodeWatch,
    private val removeEpisodeWatches: RemoveEpisodeWatches,
    private val removeEpisodeWatch: RemoveEpisodeWatch,
    tmdbManager: TmdbManager
) : TiviMvRxViewModel<EpisodeDetailsViewState>(initialState) {
    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: EpisodeDetailsViewState): EpisodeDetailsViewModel
    }

    companion object : MvRxViewModelFactory<EpisodeDetailsViewState> {
        @JvmStatic
        override fun create(activity: FragmentActivity, state: EpisodeDetailsViewState): EpisodeDetailsViewModel {
            return (activity as ShowDetailsActivity).episodeDetailsViewModelFactory.create(state)
        }
    }

    init {
        updateEpisodeDetails.observe()
                .toObservable()
                .subscribeOn(schedulers.io)
                .execute { copy(episode = it) }

        updateEpisodeWatches.observe()
                .toObservable()
                .subscribeOn(schedulers.io)
                .execute {
                    val action = if (it is Success && it()!!.isNotEmpty()) {
                        Action.UNWATCH
                    } else {
                        Action.WATCH
                    }
                    copy(watches = it, action = action)
                }

        tmdbManager.imageProviderObservable
                .delay(50, TimeUnit.MILLISECONDS, schedulers.io)
                .subscribeOn(schedulers.io)
                .execute { copy(tmdbImageUrlProvider = it) }

        withState {
            updateEpisodeDetails.setParams(UpdateEpisodeDetails.Params(it.episodeId))
            updateEpisodeWatches.setParams(UpdateEpisodeWatches.Params(it.episodeId))
        }

        refresh()
    }

    private fun refresh() {
        launchInteractor(updateEpisodeDetails, UpdateEpisodeDetails.ExecuteParams(true))
        launchInteractor(updateEpisodeWatches, UpdateEpisodeWatches.ExecuteParams(true))
    }

    fun removeWatchEntry(entry: EpisodeWatchEntry) {
        launchInteractor(removeEpisodeWatch, RemoveEpisodeWatch.Params(entry.id))
    }

    fun markWatched() {
        withState {
            launchInteractor(addEpisodeWatch, AddEpisodeWatch.Params(it.episodeId, OffsetDateTime.now()))
        }
    }

    fun markUnwatched() {
        withState {
            launchInteractor(removeEpisodeWatches, RemoveEpisodeWatches.Params(it.episodeId))
        }
    }
}