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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import app.tivi.SharedElementHelper
import app.tivi.data.entities.Episode
import app.tivi.data.entities.TiviShow
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.interactors.FollowShow
import app.tivi.interactors.UnfollowShow
import app.tivi.interactors.UpdateFollowedShowSeasonData
import app.tivi.interactors.UpdateRelatedShows
import app.tivi.interactors.UpdateShowDetails
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
    private val updateShowDetails: UpdateShowDetails,
    private val updateRelatedShows: UpdateRelatedShows,
    private val updateShowSeasons: UpdateFollowedShowSeasonData,
    private val tmdbManager: TmdbManager,
    private val followShow: FollowShow,
    private val unfollowShow: UnfollowShow,
    private val followedShowsRepository: FollowedShowsRepository,
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
                    _data.value = null
                }
            }
        }

    private val _data = MutableLiveData<ShowDetailsViewState>()
    val data: LiveData<ShowDetailsViewState>
        get() = _data

    private fun refresh() {
        showId?.also { id ->
            launchInteractor(updateShowDetails, UpdateShowDetails.Params(id, true))
            launchInteractor(updateRelatedShows, UpdateRelatedShows.Params(id, true))
            launchInteractor(updateShowSeasons, UpdateFollowedShowSeasonData.Params(id, true))
        }
    }

    private fun setupLiveData() {
        showId?.also { id ->
            disposables += followedShowsRepository.observeIsShowFollowed(id)
                    .subscribeOn(schedulers.io)
                    .distinctUntilChanged()
                    .switchMap { isFollowed ->
                        when {
                            isFollowed -> Flowables.combineLatest(
                                    updateShowDetails.observe(),
                                    updateRelatedShows.observe(),
                                    updateShowSeasons.observe(),
                                    tmdbManager.imageProvider,
                                    ::FollowedShowDetailsViewState)
                            else -> Flowables.combineLatest(
                                    updateShowDetails.observe(),
                                    updateRelatedShows.observe(),
                                    tmdbManager.imageProvider,
                                    ::NotFollowedShowDetailsViewState)
                        }
                    }
                    .observeOn(schedulers.main)
                    .subscribe(_data::setValue, logger::e)
        }
    }

    fun addToMyShows() {
        showId?.also { id -> launchInteractor(followShow, FollowShow.Params(id, true)) }
    }

    override fun onCleared() {
        super.onCleared()
        updateShowDetails.clear()
    }

    fun removeFromMyShows() {
        showId?.let { id -> launchInteractor(unfollowShow, UnfollowShow.Params(id, true)) }
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