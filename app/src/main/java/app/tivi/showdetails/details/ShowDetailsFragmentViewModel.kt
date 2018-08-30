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

import android.support.v4.app.FragmentActivity
import app.tivi.SharedElementHelper
import app.tivi.TiviActivity
import app.tivi.data.entities.ActionDate
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.interactors.ChangeSeasonWatchedStatus
import app.tivi.interactors.ChangeSeasonWatchedStatus.Action
import app.tivi.interactors.ChangeSeasonWatchedStatus.Params
import app.tivi.interactors.ChangeShowFollowStatus
import app.tivi.interactors.ChangeShowFollowStatus.Action.TOGGLE
import app.tivi.interactors.UpdateFollowedShowSeasonData
import app.tivi.interactors.UpdateRelatedShows
import app.tivi.interactors.UpdateShowDetails
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppRxSchedulers
import app.tivi.util.TiviMvRxViewModel
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import javax.inject.Inject

class ShowDetailsFragmentViewModel @Inject constructor(
    schedulers: AppRxSchedulers,
    private val updateShowDetails: UpdateShowDetails,
    private val updateRelatedShows: UpdateRelatedShows,
    private val updateShowSeasons: UpdateFollowedShowSeasonData,
    private val changeSeasonWatchedStatus: ChangeSeasonWatchedStatus,
    tmdbManager: TmdbManager,
    private val changeShowFollowStatus: ChangeShowFollowStatus
) : TiviMvRxViewModel<ShowDetailsViewState>(ShowDetailsViewState(0)) {

    companion object : MvRxViewModelFactory<ShowDetailsViewState> {
        @JvmStatic
        override fun create(activity: FragmentActivity, state: ShowDetailsViewState): ShowDetailsFragmentViewModel {
            return (activity as TiviActivity).viewModelFactory.create(ShowDetailsFragmentViewModel::class.java)
                    .apply {
                        // We can't use assisted DI with Dagger so we manually update the state
                        setState { state }
                        refresh()
                    }
        }
    }

    private fun refresh() {
        withState {
            updateShowDetails.setParams(UpdateShowDetails.Params(it.showId))
            updateRelatedShows.setParams(UpdateRelatedShows.Params(it.showId))
            updateShowSeasons.setParams(UpdateFollowedShowSeasonData.Params(it.showId))
            changeShowFollowStatus.setParams(ChangeShowFollowStatus.Params(it.showId))
        }

        launchInteractor(updateShowDetails, UpdateShowDetails.ExecuteParams(true))
        launchInteractor(updateRelatedShows, UpdateRelatedShows.ExecuteParams(true))
        launchInteractor(updateShowSeasons, UpdateFollowedShowSeasonData.ExecuteParams(true))
    }

    init {
        changeShowFollowStatus.observe()
                .toObservable()
                .observeOn(schedulers.io)
                .execute {
                    when (it) {
                        is Success -> copy(isFollowed = it.invoke()!!)
                        else -> copy(isFollowed = false)
                    }
                }

        updateShowDetails.observe()
                .toObservable()
                .observeOn(schedulers.io)
                .execute { copy(show = it) }

        updateRelatedShows.observe()
                .toObservable()
                .observeOn(schedulers.io)
                .execute { copy(relatedShows = it) }

        tmdbManager.imageProvider
                .toObservable()
                .observeOn(schedulers.io)
                .execute { copy(tmdbImageUrlProvider = it) }

        updateShowSeasons.observe()
                .toObservable()
                .observeOn(schedulers.io)
                .execute { copy(seasons = it) }
    }

    override fun onCleared() {
        super.onCleared()
        updateShowDetails.clear()
    }

    fun onToggleMyShowsButtonClicked() {
        launchInteractor(changeShowFollowStatus, ChangeShowFollowStatus.ExecuteParams(TOGGLE))
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

    fun onMarkSeasonWatched(season: Season, onlyAired: Boolean, date: ActionDate) {
        launchInteractor(changeSeasonWatchedStatus,
                Params(season.id, ChangeSeasonWatchedStatus.Action.WATCHED, onlyAired, date))
    }

    fun onMarkSeasonUnwatched(season: Season) {
        launchInteractor(changeSeasonWatchedStatus, Params(season.id, Action.UNWATCH))
    }

    fun onUpClicked(showDetailsNavigator: ShowDetailsNavigator) = showDetailsNavigator.navigateUp()
}