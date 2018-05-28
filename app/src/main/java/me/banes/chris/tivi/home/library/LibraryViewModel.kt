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
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import me.banes.chris.tivi.SharedElementHelper
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.home.HomeFragmentViewModel
import me.banes.chris.tivi.home.HomeNavigator
import me.banes.chris.tivi.tmdb.TmdbManager
import me.banes.chris.tivi.trakt.TraktAuthState
import me.banes.chris.tivi.trakt.TraktManager
import me.banes.chris.tivi.trakt.calls.FollowedShowsCall
import me.banes.chris.tivi.trakt.calls.WatchedShowsCall
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.Logger
import me.banes.chris.tivi.util.NetworkDetector
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    schedulers: AppRxSchedulers,
    private val watchedShowsCall: WatchedShowsCall,
    private val followedShowsCall: FollowedShowsCall,
    private val traktManager: TraktManager,
    tmdbManager: TmdbManager,
    private val networkDetector: NetworkDetector,
    logger: Logger
) : HomeFragmentViewModel(traktManager, logger) {
    val data = MutableLiveData<LibraryViewState>()

    init {
        disposables += Flowables.combineLatest(
                watchedShowsCall.data().map { it.take(20) },
                followedShowsCall.data().map { it.take(20) },
                tmdbManager.imageProvider,
                ::LibraryViewState)
                .observeOn(schedulers.main)
                .subscribe(data::setValue, logger::e)

        refresh()
    }

    private fun refresh() {
        disposables += Observables.combineLatest(
                networkDetector.waitForConnection().toObservable(),
                traktManager.state.filter { it == TraktAuthState.LOGGED_IN }
        ).subscribe({ onRefresh() }, logger::e)
    }

    private fun onRefresh() {
        refreshWatched()
    }

    private fun refreshWatched() {
        launchWithParent {
            try {
                watchedShowsCall.refresh(Unit)
            } catch (e: Exception) {
                logger.e(e, "Error while refreshing watched shows")
            }
        }
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
