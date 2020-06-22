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
import app.tivi.ui.SnackbarManager
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class ShowDetailsFragmentViewModel @AssistedInject constructor(
    @Assisted initialState: ShowDetailsViewState,
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
) : ReduxViewModel<ShowDetailsViewState>(initialState) {
    private val loadingState = ObservableLoadingCounter()

    private val pendingActions = Channel<ShowDetailsAction>(Channel.BUFFERED)

    init {
        viewModelScope.launch {
            observeShowFollowStatus.observe()
                .distinctUntilChanged()
                .execute { copy(isFollowed = if (it is Success) it() else false) }
        }

        viewModelScope.launch {
            observeShowDetails.observe()
                .distinctUntilChanged()
                .execute {
                    if (it is Success) {
                        copy(show = it())
                    } else this
                }
        }

        viewModelScope.launch {
            observeShowImages.observe()
                .distinctUntilChanged()
                .execute { images ->
                    if (images is Success) {
                        copy(backdropImage = images().backdrop, posterImage = images().poster)
                    } else this
                }
        }

        viewModelScope.launch {
            loadingState.observable
                .collect { setState { copy(refreshing = it) } }
        }

        viewModelScope.launch {
            observeRelatedShows.observe()
                .distinctUntilChanged()
                .execute { copy(relatedShows = it) }
        }

        viewModelScope.launch {
            observeNextEpisodeToWatch.observe()
                .distinctUntilChanged()
                .execute { copy(nextEpisodeToWatch = it) }
        }

        viewModelScope.launch {
            observeShowSeasons.observe()
                .distinctUntilChanged()
                .execute { copy(seasons = it) }
        }

        viewModelScope.launch {
            observeShowViewStats.observe()
                .distinctUntilChanged()
                .execute { copy(viewStats = it) }
        }

        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
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
        }

        snackbarManager.launchInScope(viewModelScope) { uiError, visible ->
            viewModelScope.setState {
                copy(refreshError = if (visible) uiError else null)
            }
        }

        selectSubscribe(ShowDetailsViewState::showId) { showId ->
            observeShowFollowStatus(ObserveShowFollowStatus.Params(showId))
            observeShowDetails(ObserveShowDetails.Params(showId))
            observeShowImages(ObserveShowImages.Params(showId))
            observeRelatedShows(ObserveRelatedShows.Params(showId))
            observeShowSeasons(ObserveShowSeasonData.Params(showId))
            observeNextEpisodeToWatch(ObserveShowNextEpisodeToWatch.Params(showId))
            observeShowViewStats(ObserveShowViewStats.Params(showId))

            refresh(false)
        }
    }

    private fun refresh(fromUser: Boolean) {
        viewModelScope.withState { state ->
            updateShowDetails(UpdateShowDetails.Params(state.showId, fromUser)).watchStatus()
            updateShowImages(UpdateShowImages.Params(state.showId, fromUser)).watchStatus()
            updateRelatedShows(UpdateRelatedShows.Params(state.showId, fromUser)).watchStatus()
            updateShowSeasons(UpdateShowSeasonData.Params(state.showId, fromUser)).watchStatus()
        }
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

    fun submitAction(action: ShowDetailsAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                pendingActions.send(action)
            }
        }
    }

    private fun onToggleMyShowsButtonClicked() {
        viewModelScope.setState {
            copy(refreshError = UiError(IllegalArgumentException("Error to show a Snackbar")))
            // changeShowFollowStatus(ChangeShowFollowStatus.Params(state.showId, TOGGLE)).watchStatus()
        }
    }

    private fun openShowDetails(action: OpenShowDetails) {
        viewModelScope.setState {
            val pending = pendingUiEffects.filter { it !is OpenShowUiEffect }
            copy(pendingUiEffects = pending + OpenShowUiEffect(action.showId))
        }
    }

    private fun openEpisodeDetails(action: OpenEpisodeDetails) = viewModelScope.launch {
        val episode = getEpisode(GetEpisodeDetails.Params(action.episodeId)).first()
        if (episode != null) {
            setState {
                val pending = pendingUiEffects.filterNot { it is OpenEpisodeUiEffect }
                copy(
                    expandedSeasonIds = expandedSeasonIds + episode.seasonId,
                    pendingUiEffects = pending +
                        OpenEpisodeUiEffect(action.episodeId, episode.seasonId)
                )
            }
        }
    }

    private fun clearPendingUiEffect(action: ClearPendingUiEffect) {
        viewModelScope.setState {
            copy(pendingUiEffects = pendingUiEffects - action.effect)
        }
    }

    private fun onMarkSeasonWatched(action: MarkSeasonWatchedAction) {
        changeSeasonWatchedStatus(Params(action.seasonId, Action.WATCHED, action.onlyAired, action.date))
            .watchStatus()
    }

    private fun onMarkSeasonUnwatched(action: MarkSeasonUnwatchedAction) {
        changeSeasonWatchedStatus(Params(action.seasonId, Action.UNWATCH)).watchStatus()
    }

    private fun onChangeSeasonExpandState(seasonId: Long, expanded: Boolean) {
        viewModelScope.setState {
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

    /**
     * Factory to allow assisted injection of [ShowDetailsFragmentViewModel] with an initial state.
     */
    @AssistedInject.Factory
    internal interface Factory {
        fun create(initialState: ShowDetailsViewState): ShowDetailsFragmentViewModel
    }
}

internal fun ShowDetailsFragmentViewModel.Factory.create(
    showId: Long,
    pendingEpisodeId: Long? = null
): ShowDetailsFragmentViewModel {
    val initialState = ShowDetailsViewState(showId = showId)
    return create(initialState).apply {
        if (pendingEpisodeId != null) {
            submitAction(OpenEpisodeDetails(pendingEpisodeId))
        }
    }
}
