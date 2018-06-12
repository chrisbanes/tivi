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

package app.tivi.showdetails.details

import android.arch.lifecycle.MutableLiveData
import app.tivi.SharedElementHelper
import app.tivi.actions.ShowTasks
import app.tivi.calls.SyncShowWatchedEpisodesCall
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.entities.Episode
import app.tivi.data.entities.TiviShow
import app.tivi.datasources.trakt.RelatedShowsDataSource
import app.tivi.datasources.trakt.ShowDetailsDataSource
import app.tivi.datasources.trakt.ShowSeasonsDataSource
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppRxSchedulers
import app.tivi.util.Logger
import app.tivi.util.TiviViewModel
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class ShowDetailsFragmentViewModel @Inject constructor(
    private val schedulers: AppRxSchedulers,
    private val showCall: ShowDetailsDataSource,
    private val relatedShows: RelatedShowsDataSource,
    private val seasonsCall: ShowSeasonsDataSource,
    private val showWatchedEpisodesCall: SyncShowWatchedEpisodesCall,
    private val tmdbManager: TmdbManager,
    private val showTasks: ShowTasks,
    private val followedShowsDao: FollowedShowsDao,
    private val logger: Logger
) : TiviViewModel() {

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

    val data = MutableLiveData<ShowDetailsViewState>()

    private fun refresh() {
        showId?.let { id ->
            launchWithParent {
                showCall.refresh(id)
            }
            launchWithParent {
                relatedShows.refresh(id)
            }
            launchWithParent {
                if (followedShowsDao.entryCountWithShowId(id) > 0) {
                    showWatchedEpisodesCall.doWork(id)
                }
            }
        }
    }

    private fun setupLiveData() {
        showId?.let { id ->
            disposables += followedShowsDao.entryCountWithShowIdFlowable(id)
                    .subscribeOn(schedulers.database)
                    .flatMap {
                        if (it > 0) {
                            // Followed show
                            Flowables.combineLatest(
                                    showCall.data(id),
                                    relatedShows.data(id),
                                    seasonsCall.data(id),
                                    tmdbManager.imageProvider,
                                    ::FollowedShowDetailsViewState)
                        } else {
                            // Not followed
                            Flowables.combineLatest(
                                    showCall.data(id),
                                    relatedShows.data(id),
                                    tmdbManager.imageProvider,
                                    ::NotFollowedShowDetailsViewState)
                        }
                    }
                    .observeOn(schedulers.main)
                    .subscribe(data::setValue, logger::e)
        }
    }

    fun addToMyShows() {
        showId?.let {
            showTasks.followShow(it)
        }
    }

    fun removeFromMyShows() {
        showId?.let {
            showTasks.unfollowShow(it)
        }
    }

    fun onRelatedShowClicked(
        showDetailsNavigator: ShowDetailsNavigator,
        show: TiviShow,
        sharedElementHelper: SharedElementHelper? = null
    ) = showDetailsNavigator.showShowDetails(show, sharedElementHelper)

    fun onRelatedShowClicked(
        showDetailsNavigator: ShowDetailsNavigator,
        episode: Episode
    ) = showDetailsNavigator.showEpisodeDetails(episode)
}