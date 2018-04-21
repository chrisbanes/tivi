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

package me.banes.chris.tivi.showdetails.details

import android.arch.lifecycle.MutableLiveData
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.experimental.launch
import me.banes.chris.tivi.SharedElementHelper
import me.banes.chris.tivi.actions.TiviActions
import me.banes.chris.tivi.data.daos.MyShowsDao
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.showdetails.ShowDetailsNavigator
import me.banes.chris.tivi.tmdb.TmdbManager
import me.banes.chris.tivi.trakt.calls.RelatedShowsCall
import me.banes.chris.tivi.trakt.calls.ShowDetailsCall
import me.banes.chris.tivi.trakt.calls.ShowSeasonsCall
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.RxAwareViewModel
import timber.log.Timber
import javax.inject.Inject

class ShowDetailsFragmentViewModel @Inject constructor(
    private val schedulers: AppRxSchedulers,
    private val showCall: ShowDetailsCall,
    private val relatedShows: RelatedShowsCall,
    private val seasonsCall: ShowSeasonsCall,
    private val tmdbManager: TmdbManager,
    private val tiviActions: TiviActions,
    private val myShowsDao: MyShowsDao
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
        showId?.let { id ->
            launch {
                showCall.refresh(id)
            }
            launch {
                seasonsCall.refresh(id)
            }
            launch {
                relatedShows.refresh(id)
            }
        }
    }

    private fun setupLiveData() {
        showId?.let {
            disposables += Flowables.combineLatest(
                    showCall.data(it),
                    relatedShows.data(it),
                    seasonsCall.data(it),
                    tmdbManager.imageProvider,
                    myShowsDao.showEntry(it).map { it > 0 },
                    ::ShowDetailsFragmentViewState)
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

    fun addToMyShows() {
        showId?.let {
            tiviActions.addShowToMyShows(it)
        }
    }

    fun removeFromMyShows() {
        showId?.let {
            tiviActions.removeShowFromMyShows(it)
        }
    }

    fun onRelatedShowClicked(
        navigatorShow: ShowDetailsNavigator,
        show: TiviShow,
        sharedElementHelper: SharedElementHelper? = null
    ) {
        navigatorShow.showShowDetails(show, sharedElementHelper)
    }
}