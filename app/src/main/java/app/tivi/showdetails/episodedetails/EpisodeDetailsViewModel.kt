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
import app.tivi.tmdb.TmdbManager
import app.tivi.datasources.trakt.EpisodeDetailsDataSource
import app.tivi.datasources.trakt.EpisodeWatchesDataSource
import app.tivi.util.Logger
import app.tivi.util.TiviViewModel
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class EpisodeDetailsViewModel @Inject constructor(
    private val episodeDetailsCall: EpisodeDetailsDataSource,
    private val episodeWatchesCall: EpisodeWatchesDataSource,
    private val tmdbManager: TmdbManager,
    private val logger: Logger
) : TiviViewModel() {

    var episodeId: Long? = null
        set(value) {
            if (field != value) {
                field = value
                setupLiveData(value!!)
                refresh()
            }
        }

    private val _data = MutableLiveData<EpisodeDetailsViewState>()
    val data: LiveData<EpisodeDetailsViewState>
        get() = _data

    private fun refresh() {
        val epId = episodeId
        if (epId != null) {
            launchWithParent {
                episodeDetailsCall.refresh(epId)
            }
        } else {
            _data.value = null
        }
    }

    private fun setupLiveData(episodeId: Long) {
        disposables.clear()

        val watches = episodeWatchesCall.data(episodeId)

        disposables += Flowables.combineLatest(
                episodeDetailsCall.data(episodeId),
                watches,
                tmdbManager.imageProvider,
                watches.map {
                    if (it.isEmpty()) {
                        EpisodeDetailsViewState.Action.WATCH
                    } else {
                        EpisodeDetailsViewState.Action.UNWATCH
                    }
                },
                ::EpisodeDetailsViewState
        ).subscribe(_data::postValue, logger::e)
    }

    fun markWatched() {
        // TODO
    }

    fun markUnwatched() {
        // TODO
    }
}