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
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.entities.Episode
import app.tivi.data.entities.TiviShow
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.tmdb.TmdbManager
import app.tivi.trakt.calls.RelatedShowsCall
import app.tivi.trakt.calls.ShowDetailsCall
import app.tivi.trakt.calls.ShowSeasonsCall
import app.tivi.util.AppRxSchedulers
import app.tivi.util.Logger
import app.tivi.util.TiviViewModel
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class ShowDetailsFragmentViewModel @Inject constructor(
    private val schedulers: AppRxSchedulers,
    private val showCall: ShowDetailsCall,
    private val relatedShows: RelatedShowsCall,
    private val seasonsCall: ShowSeasonsCall,
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

            // TODO re-add some sort of season refresh
            showTasks.syncShowWatchedEpisodes(id)

            launchWithParent {
                relatedShows.refresh(id)
            }
        }
    }

    private fun setupLiveData() {
        showId?.let { id ->
            disposables += followedShowsDao.entryCountWithShowId(id)
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

    private fun onRefreshSuccess() {
        // TODO nothing really to do here
    }

    private fun onRefreshError(t: Throwable) = logger.e(t, "Error while refreshing")

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

    fun showEpisodeDetails(
        showDetailsNavigator: ShowDetailsNavigator,
        episode: Episode
    ) = showDetailsNavigator.showEpisodeDetails(episode)
}