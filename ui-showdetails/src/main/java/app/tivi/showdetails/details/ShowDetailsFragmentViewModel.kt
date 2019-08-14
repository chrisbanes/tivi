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
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.domain.execute
import app.tivi.domain.interactors.ChangeSeasonFollowStatus
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus.Action
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus.Params
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.ChangeShowFollowStatus.Action.TOGGLE
import app.tivi.domain.interactors.UpdateRelatedShows
import app.tivi.domain.interactors.UpdateShowDetails
import app.tivi.domain.interactors.UpdateShowSeasonData
import app.tivi.domain.launchInteractor
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObserveRelatedShows
import app.tivi.domain.observers.ObserveShowDetails
import app.tivi.domain.observers.ObserveShowFollowStatus
import app.tivi.domain.observers.ObserveShowNextEpisodeToWatch
import app.tivi.domain.observers.ObserveShowSeasonData
import app.tivi.inject.ProcessLifetime
import app.tivi.showdetails.ShowDetailsNavigator
import app.tivi.tmdb.TmdbManager
import app.tivi.util.ObservableLoadingCounter
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
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
    tmdbManager: TmdbManager,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    private val changeSeasonFollowStatus: ChangeSeasonFollowStatus,
    @ProcessLifetime private val dataOperationScope: CoroutineScope
) : TiviMvRxViewModel<ShowDetailsViewState>(initialState) {

    private val loadingState = ObservableLoadingCounter()

    init {
        viewModelScope.launchObserve(observeShowFollowStatus) {
            it.distinctUntilChanged().execute { result ->
                when (result) {
                    is Success -> copy(isFollowed = result())
                    else -> copy(isFollowed = false)
                }
            }
        }

        viewModelScope.launchObserve(observeShowDetails) {
            it.distinctUntilChanged().execute { result ->
                if (result is Success) {
                    val value = result()
                    copy(show = value.show, posterImage = value.poster, backdropImage = value.backdrop)
                } else {
                    this
                }
            }
        }

        viewModelScope.launch {
            loadingState.observable.collect { setState { copy(refreshing = it) } }
        }

        viewModelScope.launchObserve(observeRelatedShows) {
            it.distinctUntilChanged().execute { result ->
                copy(relatedShows = result)
            }
        }

        viewModelScope.launchObserve(observeNextEpisodeToWatch) {
            it.distinctUntilChanged()
                    .execute { copy(nextEpisodeToWatch = it) }
        }

        viewModelScope.launch {
            tmdbManager.imageProviderFlow
                    .execute { copy(tmdbImageUrlProvider = it) }
        }

        viewModelScope.launchObserve(observeShowSeasons) {
            it.distinctUntilChanged().execute { result ->
                copy(seasons = result)
            }
        }

        withState {
            viewModelScope.launchInteractor(observeShowFollowStatus,
                    ObserveShowFollowStatus.Params(it.showId))
            viewModelScope.launchInteractor(
                    observeShowDetails, ObserveShowDetails.Params(it.showId))
            viewModelScope.launchInteractor(observeRelatedShows,
                    ObserveRelatedShows.Params(it.showId))
            viewModelScope.launchInteractor(observeShowSeasons,
                    ObserveShowSeasonData.Params(it.showId))
            viewModelScope.launchInteractor(observeNextEpisodeToWatch,
                    ObserveShowNextEpisodeToWatch.Params(it.showId))
        }

        refresh(false)
    }

    fun refresh(fromUserInteraction: Boolean) = withState {
        dataOperationScope.launchInteractor(
                updateShowDetails,
                UpdateShowDetails.Params(it.showId, fromUserInteraction),
                loadingState
        )
        dataOperationScope.launchInteractor(
                updateRelatedShows,
                UpdateRelatedShows.Params(it.showId, fromUserInteraction),
                loadingState
        )
        dataOperationScope.launchInteractor(
                updateShowSeasons,
                UpdateShowSeasonData.Params(it.showId, fromUserInteraction),
                loadingState
        )
    }

    fun onToggleMyShowsButtonClicked() = withState {
        dataOperationScope.launch {
            changeShowFollowStatus.execute(
                    ChangeShowFollowStatus.Params(it.showId, TOGGLE),
                    loadingState
            )
            updateShowSeasons.execute(
                    UpdateShowSeasonData.Params(it.showId, false),
                    loadingState
            )
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
        dataOperationScope.launchInteractor(changeSeasonWatchedStatus,
                Params(season.id, Action.WATCHED, onlyAired, date))
    }

    fun onMarkSeasonUnwatched(season: Season) {
        dataOperationScope.launchInteractor(changeSeasonWatchedStatus,
                Params(season.id, Action.UNWATCH))
    }

    fun toggleSeasonExpanded(season: Season) = setState {
        val newExpandedSeason = if (expandedSeasonIds.contains(season.id)) {
            expandedSeasonIds - season.id
        } else {
            expandedSeasonIds + season.id
        }
        copy(expandedSeasonIds = newExpandedSeason)
    }

    fun onMarkSeasonFollowed(season: Season) {
        dataOperationScope.launchInteractor(changeSeasonFollowStatus,
                ChangeSeasonFollowStatus.Params(season.id, ChangeSeasonFollowStatus.Action.FOLLOW))
    }

    fun onMarkSeasonIgnored(season: Season) {
        dataOperationScope.launchInteractor(changeSeasonFollowStatus,
                ChangeSeasonFollowStatus.Params(season.id, ChangeSeasonFollowStatus.Action.IGNORE))
    }

    fun onMarkPreviousSeasonsIgnored(season: Season) {
        dataOperationScope.launchInteractor(changeSeasonFollowStatus,
                ChangeSeasonFollowStatus.Params(season.id,
                        ChangeSeasonFollowStatus.Action.IGNORE_PREVIOUS))
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