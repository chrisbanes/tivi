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
import app.tivi.TiviMvRxViewModel
import app.tivi.data.entities.ActionDate
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.domain.interactors.ChangeSeasonFollowStatus
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus.Action
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus.Params
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.ChangeShowFollowStatus.Action.TOGGLE
import app.tivi.domain.interactors.UpdateRelatedShows
import app.tivi.domain.interactors.UpdateShowDetails
import app.tivi.domain.interactors.UpdateShowSeasonData
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObserveRelatedShows
import app.tivi.domain.observers.ObserveShowDetails
import app.tivi.domain.observers.ObserveShowFollowStatus
import app.tivi.domain.observers.ObserveShowNextEpisodeToWatch
import app.tivi.domain.observers.ObserveShowSeasonData
import app.tivi.domain.observers.ObserveShowViewStats
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectFrom
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ShowDetailsFragmentViewModel @AssistedInject constructor(
    @Assisted initialState: ShowDetailsViewState,
    private val updateShowDetails: UpdateShowDetails,
    observeShowDetails: ObserveShowDetails,
    private val updateRelatedShows: UpdateRelatedShows,
    observeRelatedShows: ObserveRelatedShows,
    private val updateShowSeasons: UpdateShowSeasonData,
    observeShowSeasons: ObserveShowSeasonData,
    private val changeSeasonWatchedStatus: ChangeSeasonWatchedStatus,
    observeShowFollowStatus: ObserveShowFollowStatus,
    observeNextEpisodeToWatch: ObserveShowNextEpisodeToWatch,
    observeShowViewStats: ObserveShowViewStats,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    private val changeSeasonFollowStatus: ChangeSeasonFollowStatus
) : TiviMvRxViewModel<ShowDetailsViewState>(initialState) {

    private val loadingState = ObservableLoadingCounter()

    init {
        viewModelScope.launchObserve(observeShowFollowStatus) { flow ->
            flow.distinctUntilChanged().execute {
                copy(isFollowed = if (it is Success) it() else false)
            }
        }

        viewModelScope.launchObserve(observeShowDetails) { flow ->
            flow.distinctUntilChanged().execute {
                if (it is Success) {
                    val value = it()
                    copy(show = value.show, posterImage = value.poster, backdropImage = value.backdrop)
                } else {
                    this
                }
            }
        }

        viewModelScope.launch {
            loadingState.observable.collect { setState { copy(refreshing = it) } }
        }

        viewModelScope.launchObserve(observeRelatedShows) { flow ->
            flow.distinctUntilChanged().execute { copy(relatedShows = it) }
        }

        viewModelScope.launchObserve(observeNextEpisodeToWatch) { flow ->
            flow.distinctUntilChanged().execute { copy(nextEpisodeToWatch = it) }
        }

        viewModelScope.launchObserve(observeShowSeasons) { flow ->
            flow.distinctUntilChanged().execute { copy(seasons = it) }
        }

        viewModelScope.launchObserve(observeShowViewStats) { flow ->
            flow.distinctUntilChanged().execute { copy(viewStats = it) }
        }

        withState {
            observeShowFollowStatus(ObserveShowFollowStatus.Params(it.showId))
            observeShowDetails(ObserveShowDetails.Params(it.showId))
            observeRelatedShows(ObserveRelatedShows.Params(it.showId))
            observeShowSeasons(ObserveShowSeasonData.Params(it.showId))
            observeNextEpisodeToWatch(ObserveShowNextEpisodeToWatch.Params(it.showId))
            observeShowViewStats(ObserveShowViewStats.Params(it.showId))
        }

        refresh(false)
    }

    fun refresh(fromUserInteraction: Boolean) = withState {
        updateShowDetails(UpdateShowDetails.Params(it.showId, fromUserInteraction)).also {
            viewModelScope.launch {
                loadingState.collectFrom(it)
            }
        }
        updateRelatedShows(UpdateRelatedShows.Params(it.showId, fromUserInteraction)).also {
            viewModelScope.launch {
                loadingState.collectFrom(it)
            }
        }
        updateShowSeasons(UpdateShowSeasonData.Params(it.showId, fromUserInteraction)).also {
            viewModelScope.launch {
                loadingState.collectFrom(it)
            }
        }
    }

    fun onToggleMyShowsButtonClicked() = withState {
        changeShowFollowStatus(ChangeShowFollowStatus.Params(it.showId, TOGGLE)).also {
            viewModelScope.launch {
                loadingState.collectFrom(it)
            }
        }
    }

    fun onRelatedShowClicked(
        showDetailsNavigator: ShowDetailsNavigator,
        show: TiviShow,
        sharedElementHelper: SharedElementHelper? = null
    ) {
        showDetailsNavigator.showShowDetails(show, sharedElementHelper)
    }

    fun onMarkSeasonWatched(season: Season, onlyAired: Boolean, date: ActionDate) {
        changeSeasonWatchedStatus(Params(season.id, Action.WATCHED, onlyAired, date))
    }

    fun onMarkSeasonUnwatched(season: Season) {
        changeSeasonWatchedStatus(Params(season.id, Action.UNWATCH))
    }

    fun expandSeason(season: Season) = setState {
        copy(expandedSeasonIds = expandedSeasonIds.plus(season.id))
    }

    fun collapseSeason(season: Season) = setState {
        copy(expandedSeasonIds = expandedSeasonIds.minus(season.id))
    }

    fun onMarkSeasonFollowed(season: Season) {
        changeSeasonFollowStatus(
                ChangeSeasonFollowStatus.Params(season.id, ChangeSeasonFollowStatus.Action.FOLLOW)
        )
    }

    fun onMarkSeasonIgnored(season: Season) {
        changeSeasonFollowStatus(
                ChangeSeasonFollowStatus.Params(season.id, ChangeSeasonFollowStatus.Action.IGNORE)
        )
    }

    fun onMarkPreviousSeasonsIgnored(season: Season) {
        changeSeasonFollowStatus(
                ChangeSeasonFollowStatus.Params(season.id,
                        ChangeSeasonFollowStatus.Action.IGNORE_PREVIOUS)
        )
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