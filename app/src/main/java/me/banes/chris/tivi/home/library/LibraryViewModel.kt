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
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.selects.select
import me.banes.chris.tivi.SharedElementHelper
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.home.HomeFragmentViewModel
import me.banes.chris.tivi.home.HomeNavigator
import me.banes.chris.tivi.tmdb.TmdbManager
import me.banes.chris.tivi.trakt.TraktManager
import me.banes.chris.tivi.trakt.calls.MyShowsCall
import me.banes.chris.tivi.trakt.calls.WatchedShowsCall
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import timber.log.Timber
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    private val watchedShowsCall: WatchedShowsCall,
    private val myShowsCall: MyShowsCall,
    traktManager: TraktManager,
    tmdbManager: TmdbManager,
    dispatchers: AppCoroutineDispatchers
) : HomeFragmentViewModel(traktManager, dispatchers) {
    val data = MutableLiveData<LibraryViewState>()

    init {
        launchWithParent(context = dispatchers.main, start = CoroutineStart.UNDISPATCHED) {
            var model = LibraryViewState()
            while (isActive) {
                model = select {
                    watchedShowsCall.data().onReceive {
                        model.copy(watched = it.take(20))
                    }
                    myShowsCall.data().onReceive {
                        model.copy(myShows = it.take(20))
                    }
                    tmdbManager.imageProvider.onReceive {
                        model.copy(tmdbImageUrlProvider = it)
                    }
                }
                data.value = model
            }
        }

        refresh()
    }

    private fun refresh() {
        launchWithParent {
            try {
                watchedShowsCall.refresh(Unit)
            } catch (e: Exception) {
                // TODO this shouldn't live here
                Timber.e(e, "Error while refreshing")
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
