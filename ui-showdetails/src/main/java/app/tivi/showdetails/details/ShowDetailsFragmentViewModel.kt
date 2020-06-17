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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import app.tivi.ReduxViewModel
import app.tivi.Success
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
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObserveRelatedShows
import app.tivi.domain.observers.ObserveShowDetails
import app.tivi.domain.observers.ObserveShowFollowStatus
import app.tivi.domain.observers.ObserveShowImages
import app.tivi.domain.observers.ObserveShowNextEpisodeToWatch
import app.tivi.domain.observers.ObserveShowSeasonData
import app.tivi.domain.observers.ObserveShowViewStats
import app.tivi.ui.SnackbarManager
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ShowDetailsFragmentViewModel @ViewModelInject constructor(
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
    private val logger: Logger
) : ReduxViewModel<ShowDetailsViewState>() {

    private val loadingState = ObservableLoadingCounter()
    private val snackbarManager = SnackbarManager()

    private val pendingActions = Channel<ShowDetailsAction>(Channel.BUFFERED)

    init {
        viewModelScope.launchObserve(observeShowFollowStatus) { flow ->
            flow.distinctUntilChanged().execute {
                copy(isFollowed = if (it is Success) it() else false)
            }
        }

        viewModelScope.launchObserve(observeShowDetails) { flow ->
            flow.distinctUntilChanged().execute {
                if (it is Success) {
                    copy(show = it())
                } else {
                    this
                }
            }
        }

        viewModelScope.launchObserve(observeShowImages) { flow ->
            flow.distinctUntilChanged().execute { images ->
                if (images is Success) {
                    copy(backdropImage = images().backdrop, posterImage = images().poster)
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

        viewModelScope.launch {
            for (action in pendingActions) when (action) {
                is RefreshAction -> refresh(true)
                FollowShowToggleAction -> onToggleMyShowsButtonClicked()
                is MarkSeasonWatchedAction -> onMarkSeasonWatched(action)
                is MarkSeasonUnwatchedAction -> onMarkSeasonUnwatched(action)
                is ChangeSeasonFollowedAction -> onChangeSeasonFollowState(action)
                is ChangeSeasonExpandedAction -> onChangeSeasonExpandState(action.seasonId, action.expanded)
                is UnfollowPreviousSeasonsFollowedAction -> onUnfollowPreviousSeasonsFollowState(action)
                is OpenEpisodeDetails -> openEpisodeDetails(action)
                is OpenShowDetails -> openShowDetails(action)
                is ClearPendingUiEffect -> clearPendingUiEffect(action)
                is ClearError -> snackbarManager.removeCurrentError()
            }
        }

        viewModelScope.launch {
            snackbarManager.launch { uiError, visible ->
                setState {
                    copy(refreshError = if (visible) uiError else null)
                }
            }
        }

        withState { state ->
            observeShowFollowStatus(ObserveShowFollowStatus.Params(state.showId))
            observeShowDetails(ObserveShowDetails.Params(state.showId))
            observeShowImages(ObserveShowImages.Params(state.showId))
            observeRelatedShows(ObserveRelatedShows.Params(state.showId))
            observeShowSeasons(ObserveShowSeasonData.Params(state.showId))
            observeNextEpisodeToWatch(ObserveShowNextEpisodeToWatch.Params(state.showId))
            observeShowViewStats(ObserveShowViewStats.Params(state.showId))

            val pendingOpenEpisode = state.pendingUiEffects
                .firstOrNull { it is PendingOpenEpisodeUiEffect }
            if (pendingOpenEpisode is PendingOpenEpisodeUiEffect) {
                openEpisodeDetails(OpenEpisodeDetails(pendingOpenEpisode.episodeId))
            }
        }

        refresh(false)
    }

    private fun refresh(fromUser: Boolean) = withState { state ->
        updateShowDetails(UpdateShowDetails.Params(state.showId, fromUser)).watchStatus()
        updateShowImages(UpdateShowImages.Params(state.showId, fromUser)).watchStatus()
        updateRelatedShows(UpdateRelatedShows.Params(state.showId, fromUser)).watchStatus()
        updateShowSeasons(UpdateShowSeasonData.Params(state.showId, fromUser)).watchStatus()
    }

    private fun Flow<InvokeStatus>.watchStatus() = viewModelScope.launch { collectStatus() }

    private suspend fun Flow<InvokeStatus>.collectStatus() = collect { status ->
        when (status) {
            InvokeStarted -> loadingState.addLoader()
            InvokeSuccess -> loadingState.removeLoader()
            is InvokeError -> {
                logger.i(status.throwable)
                snackbarManager.sendError(UiError(status.throwable))
                loadingState.removeLoader()
            }
        }
    }

    fun submitAction(action: ShowDetailsAction) = viewModelScope.launch {
        pendingActions.send(action)
    }

    private fun onToggleMyShowsButtonClicked() = withState {
        changeShowFollowStatus(ChangeShowFollowStatus.Params(it.showId, TOGGLE)).watchStatus()
    }

    private fun openShowDetails(action: OpenShowDetails) = setState {
        val pending = pendingUiEffects.filter { it !is ExecutableOpenShowUiEffect }
        copy(pendingUiEffects = pending + ExecutableOpenShowUiEffect(action.showId))
    }

    private fun openEpisodeDetails(action: OpenEpisodeDetails) = viewModelScope.launch {
        val episode = getEpisode(GetEpisodeDetails.Params(action.episodeId)).first()
        if (episode != null) {
            setState {
                val pending = pendingUiEffects.filter {
                    when (it) {
                        is PendingOpenEpisodeUiEffect,
                        is ExecutableOpenEpisodeUiEffect -> false
                        else -> true
                    }
                }
                copy(
                    expandedSeasonIds = expandedSeasonIds + episode.seasonId,
                    pendingUiEffects = pending +
                        ExecutableOpenEpisodeUiEffect(action.episodeId, episode.seasonId)
                )
            }
        }
    }

    private fun clearPendingUiEffect(action: ClearPendingUiEffect) {
        setState { copy(pendingUiEffects = pendingUiEffects - action.effect) }
    }

    private fun onMarkSeasonWatched(action: MarkSeasonWatchedAction) {
        changeSeasonWatchedStatus(Params(action.seasonId, Action.WATCHED, action.onlyAired, action.date))
            .watchStatus()
    }

    private fun onMarkSeasonUnwatched(action: MarkSeasonUnwatchedAction) {
        changeSeasonWatchedStatus(Params(action.seasonId, Action.UNWATCH))
            .watchStatus()
    }

    private fun onChangeSeasonExpandState(seasonId: Long, expanded: Boolean) = setState {
        val pending = ArrayList(pendingUiEffects)
        pending.removeAll { it is FocusSeasonUiEffect }

        if (expanded) {
            copy(
                pendingUiEffects = pending + FocusSeasonUiEffect(seasonId),
                expandedSeasonIds = expandedSeasonIds + seasonId
            )
        } else {
            copy(expandedSeasonIds = expandedSeasonIds - seasonId)
        }
    }

    private fun onChangeSeasonFollowState(action: ChangeSeasonFollowedAction) {
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

    private fun onUnfollowPreviousSeasonsFollowState(action: UnfollowPreviousSeasonsFollowedAction) {
        changeSeasonFollowStatus(
            ChangeSeasonFollowStatus.Params(
                action.seasonId,
                ChangeSeasonFollowStatus.Action.IGNORE_PREVIOUS
            )
        ).watchStatus()
    }

    override fun onCleared() {
        super.onCleared()
        pendingActions.cancel()
        snackbarManager.close()
    }

    override fun createInitialState(): ShowDetailsViewState {
        return ShowDetailsViewState()
    }
}
