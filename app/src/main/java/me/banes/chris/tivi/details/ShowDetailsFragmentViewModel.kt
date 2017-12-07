/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.details

import android.arch.lifecycle.MutableLiveData
import io.reactivex.rxkotlin.Flowables
import me.banes.chris.tivi.extensions.plusAssign
import me.banes.chris.tivi.tmdb.TmdbManager
import me.banes.chris.tivi.trakt.calls.ShowDetailsCall
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.RxAwareViewModel
import timber.log.Timber
import javax.inject.Inject

class ShowDetailsFragmentViewModel @Inject constructor(
        private val schedulers: AppRxSchedulers,
        private val showCall: ShowDetailsCall,
        private val tmdbManager: TmdbManager
) : RxAwareViewModel() {

    var showId: Long? = null
        set(value) {
            if (field != value) {
                field = value
                if (value != null) {
                    setupLiveData()
                    refresh()
                } else {
                    data.value = null
                }
            }
        }

    val data = MutableLiveData<ShowDetailsFragmentViewState>()

    private fun refresh() {
        showId?.let {
            disposables += showCall.refresh(it)
                    .subscribe(this::onRefreshSuccess, this::onRefreshError)
        }
    }

    private fun setupLiveData() {
        showId?.let {
            disposables += Flowables.combineLatest(
                    showCall.data(it),
                    tmdbManager.imageProvider,
                    { show, urlProvider ->
                        ShowDetailsFragmentViewState(show, urlProvider)
                    })
                    .observeOn(schedulers.main)
                    .subscribe(data::setValue, Timber::e)
        }
    }

    private fun onRefreshSuccess() {
        // TODO nothing really to do here
    }

    private fun onRefreshError(t: Throwable) {
        Timber.e(t, "Error while refreshing")
    }
}