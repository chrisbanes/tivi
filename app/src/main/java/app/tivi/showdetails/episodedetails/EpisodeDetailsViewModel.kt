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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.interactors.AddEpisodeWatch
import app.tivi.interactors.RemoveEpisodeWatch
import app.tivi.interactors.RemoveEpisodeWatches
import app.tivi.interactors.UpdateEpisodeDetails
import app.tivi.interactors.UpdateEpisodeWatches
import app.tivi.showdetails.episodedetails.EpisodeDetailsViewState.Action
import app.tivi.tmdb.TmdbManager
import app.tivi.util.Logger
import app.tivi.util.TiviViewModel
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.plusAssign
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

class EpisodeDetailsViewModel @Inject constructor(
    private val updateEpisodeDetails: UpdateEpisodeDetails,
    private val updateEpisodeWatches: UpdateEpisodeWatches,
    private val addEpisodeWatch: AddEpisodeWatch,
    private val removeEpisodeWatches: RemoveEpisodeWatches,
    private val removeEpisodeWatch: RemoveEpisodeWatch,
    private val tmdbManager: TmdbManager,
    private val logger: Logger
) : TiviViewModel() {

    var episodeId: Long? = null
        set(value) {
            if (field != value) {
                field = value
                if (value != null) {
                    setup(value)
                    refresh()
                }
            }
        }

    private val _data = MutableLiveData<EpisodeDetailsViewState>()
    val data: LiveData<EpisodeDetailsViewState>
        get() = _data

    fun setup(episodeId: Long) {
        updateEpisodeDetails.setParams(UpdateEpisodeDetails.Params(episodeId))
        updateEpisodeWatches.setParams(UpdateEpisodeWatches.Params(episodeId))

        disposables.clear()

        val watches = updateEpisodeWatches.observe().share()

        disposables += Flowables.combineLatest(
                updateEpisodeDetails.observe(),
                watches,
                tmdbManager.imageProvider,
                watches.map {
                    if (it.isEmpty()) { Action.WATCH } else { Action.UNWATCH }
                },
                ::EpisodeDetailsViewState
        ).subscribe(_data::postValue, logger::e)
    }

    private fun refresh() {
        val epId = episodeId
        if (epId != null) {
            launchInteractor(updateEpisodeDetails, UpdateEpisodeDetails.ExecuteParams(true))
            launchInteractor(updateEpisodeWatches, UpdateEpisodeWatches.ExecuteParams(true))
        } else {
            _data.value = null
        }
    }

    fun removeWatchEntry(entry: EpisodeWatchEntry) {
        launchInteractor(removeEpisodeWatch, RemoveEpisodeWatch.Params(entry.id!!))
    }

    fun markWatched() {
        episodeId?.also { launchInteractor(addEpisodeWatch, AddEpisodeWatch.Params(it, OffsetDateTime.now())) }
    }

    fun markUnwatched() {
        episodeId?.also { launchInteractor(removeEpisodeWatches, RemoveEpisodeWatches.Params(it)) }
    }
}