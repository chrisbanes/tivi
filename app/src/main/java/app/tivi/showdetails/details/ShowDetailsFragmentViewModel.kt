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
import app.tivi.interactors.launchInteractor
import app.tivi.showdetails.ShowDetailsActivity
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppRxSchedulers
import app.tivi.util.TiviMvRxViewModel
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import java.util.concurrent.TimeUnit

class ShowDetailsFragmentViewModel @AssistedInject constructor(
    @Assisted initialState: ShowDetailsViewState,
    schedulers: AppRxSchedulers,
    private val updateShowDetails: UpdateShowDetails,
    private val updateRelatedShows: UpdateRelatedShows,
    private val updateShowSeasons: UpdateFollowedShowSeasonData,
    private val changeSeasonWatchedStatus: ChangeSeasonWatchedStatus,
    tmdbManager: TmdbManager,
    private val changeShowFollowStatus: ChangeShowFollowStatus
) : TiviMvRxViewModel<ShowDetailsViewState>(initialState) {
    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: ShowDetailsViewState): ShowDetailsFragmentViewModel
    }

    companion object : MvRxViewModelFactory<ShowDetailsViewState> {
        @JvmStatic
        override fun create(activity: FragmentActivity, state: ShowDetailsViewState): ShowDetailsFragmentViewModel {
            return (activity as ShowDetailsActivity).showDetailsFragmentViewModelFactory.create(state)
        }
    }

    init {
        changeShowFollowStatus.observe()
                .toObservable()
                .subscribeOn(schedulers.io)
                .execute {
                    when (it) {
                        is Success -> copy(isFollowed = it.invoke()!!)
                        else -> copy(isFollowed = false)
                    }
                }

        updateShowDetails.observe()
                .toObservable()
                .subscribeOn(schedulers.io)
                .execute { copy(show = it) }

        updateRelatedShows.observe()
                .toObservable()
                .subscribeOn(schedulers.io)
                .execute { copy(relatedShows = it) }

        tmdbManager.imageProviderObservable
                .delay(50, TimeUnit.MILLISECONDS, schedulers.io)
                .subscribeOn(schedulers.io)
                .execute { copy(tmdbImageUrlProvider = it) }

        updateShowSeasons.observe()
                .toObservable()
                .subscribeOn(schedulers.io)
                .execute { copy(seasons = it) }

        withState {
            updateShowDetails.setParams(UpdateShowDetails.Params(it.showId))
            updateRelatedShows.setParams(UpdateRelatedShows.Params(it.showId))
            updateShowSeasons.setParams(UpdateFollowedShowSeasonData.Params(it.showId))
            changeShowFollowStatus.setParams(ChangeShowFollowStatus.Params(it.showId))
        }

        refresh()
    }

    private fun refresh() {
        launchInteractor(updateShowDetails, UpdateShowDetails.ExecuteParams(true))
        launchInteractor(updateRelatedShows, UpdateRelatedShows.ExecuteParams(true))
        launchInteractor(updateShowSeasons, UpdateFollowedShowSeasonData.ExecuteParams(true))
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

    fun toggleSeasonExpanded(season: Season) {
        setState {
            val newExpandedSeason = if (expandedSeasonIds.contains(season.id)) {
                expandedSeasonIds - season.id
            } else {
                expandedSeasonIds + season.id
            }
            copy(expandedSeasonIds = newExpandedSeason)
        }
    }

    fun onUpClicked(showDetailsNavigator: ShowDetailsNavigator) = showDetailsNavigator.navigateUp()
}