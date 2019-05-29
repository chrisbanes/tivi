/*
 * Copyright 2018 Google LLC
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

import androidx.lifecycle.viewModelScope
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
import app.tivi.interactors.ObserveFollowedShowSeasonData
import app.tivi.interactors.ObserveRelatedShows
import app.tivi.interactors.ObserveShowDetails
import app.tivi.interactors.ObserveShowFollowStatus
import app.tivi.interactors.UpdateFollowedShowSeasonData
import app.tivi.interactors.UpdateRelatedShows
import app.tivi.interactors.UpdateShowDetails
import app.tivi.interactors.execute
import app.tivi.interactors.launchInteractor
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppRxSchedulers
import app.tivi.util.TiviMvRxViewModel
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ShowDetailsFragmentViewModel @AssistedInject constructor(
    @Assisted initialState: ShowDetailsViewState,
    schedulers: AppRxSchedulers,
    private val updateShowDetails: UpdateShowDetails,
    observeShowDetails: ObserveShowDetails,
    private val updateRelatedShows: UpdateRelatedShows,
    observeRelatedShows: ObserveRelatedShows,
    private val updateShowSeasons: UpdateFollowedShowSeasonData,
    observeShowSeasons: ObserveFollowedShowSeasonData,
    private val changeSeasonWatchedStatus: ChangeSeasonWatchedStatus,
    observeShowFollowStatus: ObserveShowFollowStatus,
    tmdbManager: TmdbManager,
    private val changeShowFollowStatus: ChangeShowFollowStatus
) : TiviMvRxViewModel<ShowDetailsViewState>(initialState) {
    init {
        observeShowFollowStatus.observe()
                .subscribeOn(schedulers.io)
                .execute {
                    when (it) {
                        is Success -> copy(isFollowed = it.invoke()!!)
                        else -> copy(isFollowed = false)
                    }
                }

        observeShowDetails.observe()
                .subscribeOn(schedulers.io)
                .execute { copy(show = it) }

        observeRelatedShows.observe()
                .subscribeOn(schedulers.io)
                .execute { copy(relatedShows = it) }

        tmdbManager.imageProviderObservable
                .delay(50, TimeUnit.MILLISECONDS, schedulers.io)
                .subscribeOn(schedulers.io)
                .execute { copy(tmdbImageUrlProvider = it) }

        observeShowSeasons.observe()
                .subscribeOn(schedulers.io)
                .execute { copy(seasons = it) }

        withState {
            observeShowFollowStatus(ObserveShowFollowStatus.Params(it.showId))
            observeShowDetails(ObserveShowDetails.Params(it.showId))
            observeRelatedShows(ObserveRelatedShows.Params(it.showId))
            observeShowSeasons(ObserveFollowedShowSeasonData.Params(it.showId))
        }

        refresh()
    }

    private fun refresh() = withState {
        viewModelScope.launchInteractor(updateShowDetails, UpdateShowDetails.Params(it.showId, true))
        viewModelScope.launchInteractor(updateRelatedShows, UpdateRelatedShows.Params(it.showId, true))
        viewModelScope.launchInteractor(updateShowSeasons, UpdateFollowedShowSeasonData.Params(it.showId, true))
    }

    fun onToggleMyShowsButtonClicked() = withState {
        viewModelScope.launch {
            changeShowFollowStatus.execute(ChangeShowFollowStatus.Params(it.showId, TOGGLE))
            updateShowSeasons.execute(UpdateFollowedShowSeasonData.Params(it.showId, false))
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

    fun onMarkSeasonWatched(season: Season, onlyAired: Boolean, date: ActionDate) {
        viewModelScope.launchInteractor(changeSeasonWatchedStatus, Params(season.id, Action.WATCHED, onlyAired, date))
    }

    fun onMarkSeasonUnwatched(season: Season) {
        viewModelScope.launchInteractor(changeSeasonWatchedStatus, Params(season.id, Action.UNWATCH))
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

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: ShowDetailsViewState): ShowDetailsFragmentViewModel
    }

    companion object : MvRxViewModelFactory<ShowDetailsFragmentViewModel, ShowDetailsViewState> {
        override fun create(viewModelContext: ViewModelContext, state: ShowDetailsViewState): ShowDetailsFragmentViewModel? {
            val fragment: ShowDetailsFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.showDetailsViewModelFactory.create(state)
        }
    }
}