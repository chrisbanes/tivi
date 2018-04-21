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

package me.banes.chris.tivi.home.library

import android.arch.lifecycle.MutableLiveData
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.experimental.launch
import me.banes.chris.tivi.AppNavigator
import me.banes.chris.tivi.SharedElementHelper
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.home.HomeFragmentViewModel
import me.banes.chris.tivi.home.HomeNavigator
import me.banes.chris.tivi.tmdb.TmdbManager
import me.banes.chris.tivi.trakt.TraktManager
import me.banes.chris.tivi.trakt.calls.MyShowsCall
import me.banes.chris.tivi.trakt.calls.WatchedShowsCall
import me.banes.chris.tivi.util.AppRxSchedulers
import timber.log.Timber
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    schedulers: AppRxSchedulers,
    private val watchedShowsCall: WatchedShowsCall,
    private val myShowsCall: MyShowsCall,
    appNavigator: AppNavigator,
    traktManager: TraktManager,
    tmdbManager: TmdbManager
) : HomeFragmentViewModel(traktManager, appNavigator) {
    val data = MutableLiveData<LibraryViewState>()

    init {
        disposables += Flowables.combineLatest(
                watchedShowsCall.data().map { it.take(20) },
                myShowsCall.data().map { it.take(20) },
                tmdbManager.imageProvider,
                ::LibraryViewState)
                .observeOn(schedulers.main)
                .subscribe(data::setValue, Timber::e)

        refresh()
    }

    private fun refresh() {
        launch {
            watchedShowsCall.refresh(Unit)
        }
    }

    private fun onSuccess() {
        // TODO nothing really to do here
    }

    private fun onRefreshError(t: Throwable) {
        Timber.e(t, "Error while refreshing")
    }

    fun onWatchedHeaderClicked(navigator: HomeNavigator, sharedElements: SharedElementHelper) {
        navigator.showWatched(sharedElements)
    }

    fun onMyShowsHeaderClicked(navigator: HomeNavigator, sharedElements: SharedElementHelper) {
        navigator.showMyShows(sharedElements)
    }

    fun onItemPostedClicked(navigator: HomeNavigator, show: TiviShow, sharedElements: SharedElementHelper? = null) {
        navigator.showShowDetails(show, sharedElements)
    }
}
