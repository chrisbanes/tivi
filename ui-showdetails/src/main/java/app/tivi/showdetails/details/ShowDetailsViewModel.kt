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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tivi.api.UiError
import app.tivi.base.InvokeError
import app.tivi.base.InvokeStarted
import app.tivi.base.InvokeStatus
import app.tivi.base.InvokeSuccess
import app.tivi.domain.interactors.ChangeSeasonFollowStatus
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus.Action
import app.tivi.domain.interactors.ChangeSeasonWatchedStatus.Params
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.ChangeShowFollowStatus.Action.TOGGLE
import app.tivi.domain.interactors.GetEpisodeDetails
import app.tivi.domain.interactors.UpdateRelatedShows
import app.tivi.domain.interactors.UpdateShowDetails
import app.tivi.domain.interactors.UpdateShowImages
import app.tivi.domain.interactors.UpdateShowSeasonData
import app.tivi.domain.observers.ObserveRelatedShows
import app.tivi.domain.observers.ObserveShowDetails
import app.tivi.domain.observers.ObserveShowFollowStatus
import app.tivi.domain.observers.ObserveShowImages
import app.tivi.domain.observers.ObserveShowNextEpisodeToWatch
import app.tivi.domain.observers.ObserveShowSeasonData
import app.tivi.domain.observers.ObserveShowViewStats
import app.tivi.extensions.combine
import app.tivi.ui.SnackbarManager
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShowDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val updateShowDetails: UpdateShowDetails,
    observeShowDetails: ObserveShowDetails,
    observeShowImages: ObserveShowImages,
    private val updateShowImages: UpdateShowImages,
    private val updateRelatedShows: UpdateRelatedShows,
    observeRelatedShows: ObserveRelatedShows,
    private val updateShowSeasons: UpdateShowSeasonData,
    observeShowSeasons: ObserveShowSeasonData,
    private val changeSeasonWatchedStatus: ChangeSeasonWatchedStatus,
    observeShowFollowStatus: ObserveShowFollowStatus,
    observeNextEpisodeToWatch: ObserveShowNextEpisodeToWatch,
    observeShowViewStats: ObserveShowViewStats,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    private val changeSeasonFollowStatus: ChangeSeasonFollowStatus,
    private val getEpisode: GetEpisodeDetails,
    private val logger: Logger,
    private val snackbarManager: SnackbarManager
) : ViewModel() {
    private val showId: Long = savedStateHandle.get("showId")!!

    private val loadingState = ObservableLoadingCounter()

    private val pendingActions = MutableSharedFlow<ShowDetailsAction>()

    private val _uiEffects = MutableSharedFlow<UiEffect>(extraBufferCapacity = 100)

    private val expandedSeasonIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiEffects: Flow<UiEffect>
        get() = _uiEffects.asSharedFlow()

    val state = combine(
        observeShowFollowStatus.observe().distinctUntilChanged(),
        observeShowDetails.observe().distinctUntilChanged(),
        observeShowImages.observe().distinctUntilChanged(),
        loadingState.observable,
        observeRelatedShows.observe().distinctUntilChanged(),
        observeNextEpisodeToWatch.observe().distinctUntilChanged(),
        observeShowSeasons.observe().distinctUntilChanged(),
        observeShowViewStats.observe().distinctUntilChanged(),
        snackbarManager.errors,
        expandedSeasonIds,
    ) { isFollowed, show, showImages, refreshing, relatedShows, nextEpisode, seasons,
        stats, error, expandedSeasonIds ->
        ShowDetailsViewState(
            isFollowed = isFollowed,
            show = show,
            posterImage = showImages.poster,
            backdropImage = showImages.backdrop,
            relatedShows = relatedShows,
            nextEpisodeToWatch = nextEpisode,
            seasons = seasons,
            watchStats = stats,
            expandedSeasonIds = expandedSeasonIds,
            refreshing = refreshing,
            refreshError = error,
        )
    }

    init {
        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    is ShowDetailsAction.RefreshAction -> refresh(true)
                    ShowDetailsAction.FollowShowToggleAction -> onToggleMyShowsButtonClicked()
                    is ShowDetailsAction.MarkSeasonWatchedAction -> onMarkSeasonWatched(action)
                    is ShowDetailsAction.MarkSeasonUnwatchedAction -> onMarkSeasonUnwatched(action)
                    is ShowDetailsAction.ChangeSeasonFollowedAction -> onChangeSeasonFollowState(action)
                    is ShowDetailsAction.ChangeSeasonExpandedAction -> onChangeSeasonExpandState(action.seasonId, action.expanded)
                    is ShowDetailsAction.UnfollowPreviousSeasonsFollowedAction -> onUnfollowPreviousSeasonsFollowState(action)
                    is ShowDetailsAction.OpenEpisodeDetails -> openEpisodeDetails(action)
                    is ShowDetailsAction.OpenShowDetails -> openShowDetails(action)
                    is ShowDetailsAction.ClearError -> snackbarManager.removeCurrentError()
                }
            }
        }

        observeShowFollowStatus(ObserveShowFollowStatus.Params(showId))
        observeShowDetails(ObserveShowDetails.Params(showId))
        observeShowImages(ObserveShowImages.Params(showId))
        observeRelatedShows(ObserveRelatedShows.Params(showId))
        observeShowSeasons(ObserveShowSeasonData.Params(showId))
        observeNextEpisodeToWatch(ObserveShowNextEpisodeToWatch.Params(showId))
        observeShowViewStats(ObserveShowViewStats.Params(showId))

        refresh(false)
    }

    private fun refresh(fromUser: Boolean) {
        updateShowDetails(UpdateShowDetails.Params(showId, fromUser)).watchStatus()
        updateShowImages(UpdateShowImages.Params(showId, fromUser)).watchStatus()
        updateRelatedShows(UpdateRelatedShows.Params(showId, fromUser)).watchStatus()
        updateShowSeasons(UpdateShowSeasonData.Params(showId, fromUser)).watchStatus()
    }

    private fun Flow<InvokeStatus>.watchStatus() = viewModelScope.launch { collectStatus() }

    private suspend fun Flow<InvokeStatus>.collectStatus() = collect { status ->
        when (status) {
            InvokeStarted -> loadingState.addLoader()
            InvokeSuccess -> loadingState.removeLoader()
            is InvokeError -> {
                logger.i(status.throwable)
                snackbarManager.addError(UiError(status.throwable))
                loadingState.removeLoader()
            }
        }
    }

    fun submitAction(action: ShowDetailsAction) {
        viewModelScope.launch { pendingActions.emit(action) }
    }

    private fun onToggleMyShowsButtonClicked() {
        viewModelScope.launch {
            changeShowFollowStatus(ChangeShowFollowStatus.Params(showId, TOGGLE)).watchStatus()
        }
    }

    private fun openShowDetails(action: ShowDetailsAction.OpenShowDetails) {
        viewModelScope.launch {
            _uiEffects.emit(OpenShowUiEffect(action.showId))
        }
    }

    private fun openEpisodeDetails(action: ShowDetailsAction.OpenEpisodeDetails) {
        viewModelScope.launch {
            val episode = getEpisode(GetEpisodeDetails.Params(action.episodeId)).first()
            if (episode != null) {
                // Make sure the season is expanded
                expandedSeasonIds.value = expandedSeasonIds.value + episode.seasonId
                // And emit an open episode ui effect
                _uiEffects.emit(OpenEpisodeUiEffect(action.episodeId, episode.seasonId))
            }
        }
    }

    private fun onMarkSeasonWatched(action: ShowDetailsAction.MarkSeasonWatchedAction) {
        changeSeasonWatchedStatus(
            Params(action.seasonId, Action.WATCHED, action.onlyAired, action.date)
        ).watchStatus()
    }

    private fun onMarkSeasonUnwatched(action: ShowDetailsAction.MarkSeasonUnwatchedAction) {
        changeSeasonWatchedStatus(Params(action.seasonId, Action.UNWATCH)).watchStatus()
    }

    private fun onChangeSeasonExpandState(seasonId: Long, expanded: Boolean) {
        viewModelScope.launch {
            when {
                expanded -> expandedSeasonIds.value = expandedSeasonIds.value + seasonId
                else -> expandedSeasonIds.value = expandedSeasonIds.value - seasonId
            }
            if (expanded) {
                // If we've expanded, focus the season
                _uiEffects.emit(FocusSeasonUiEffect(seasonId))
            }
        }
    }

    private fun onChangeSeasonFollowState(action: ShowDetailsAction.ChangeSeasonFollowedAction) {
        // Make sure we collapse the season if it is expanded
        onChangeSeasonExpandState(action.seasonId, false)

        changeSeasonFollowStatus(
            ChangeSeasonFollowStatus.Params(
                action.seasonId,
                when {
                    action.followed -> ChangeSeasonFollowStatus.Action.FOLLOW
                    else -> ChangeSeasonFollowStatus.Action.IGNORE
                }
            )
        ).watchStatus()
    }

    private fun onUnfollowPreviousSeasonsFollowState(action: ShowDetailsAction.UnfollowPreviousSeasonsFollowedAction) {
        changeSeasonFollowStatus(
            ChangeSeasonFollowStatus.Params(
                action.seasonId,
                ChangeSeasonFollowStatus.Action.IGNORE_PREVIOUS
            )
        ).watchStatus()
    }
}
