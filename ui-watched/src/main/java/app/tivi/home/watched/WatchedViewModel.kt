/*
 * Copyright 2019 Google LLC
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

package app.tivi.home.watched

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.ReduxViewModel
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.GetTraktAuthState
import app.tivi.domain.interactors.UpdateWatchedShows
import app.tivi.domain.observers.ObservePagedWatchedShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.ShowStateSelector
import app.tivi.util.collectInto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class WatchedViewModel @Inject constructor(
    private val updateWatchedShows: UpdateWatchedShows,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    private val observePagedWatchedShows: ObservePagedWatchedShows,
    private val observeTraktAuthState: ObserveTraktAuthState,
    private val getTraktAuthState: GetTraktAuthState,
    observeUserDetails: ObserveUserDetails
) : ReduxViewModel<WatchedViewState>(
    WatchedViewState()
) {
    private val pendingActions = MutableSharedFlow<WatchedAction>()

    private val loadingState = ObservableLoadingCounter()
    private val showSelection = ShowStateSelector()

    val pagedList: Flow<PagingData<WatchedShowEntryWithShow>>
        get() = observePagedWatchedShows.observe()

    init {
        viewModelScope.launch {
            loadingState.observable
                .distinctUntilChanged()
                .debounce(2000)
                .collectAndSetState { copy(isLoading = it) }
        }

        viewModelScope.launch {
            showSelection.observeSelectedShowIds()
                .collectAndSetState { copy(selectedShowIds = it) }
        }

        viewModelScope.launch {
            showSelection.observeIsSelectionOpen()
                .collectAndSetState { copy(selectionOpen = it) }
        }

        viewModelScope.launch {
            observeTraktAuthState.observe()
                .distinctUntilChanged()
                .onEach { if (it == TraktAuthState.LOGGED_IN) refresh(false) }
                .collectAndSetState { copy(authState = it) }
        }
        observeTraktAuthState(Unit)

        viewModelScope.launch {
            observeUserDetails.observe()
                .collectAndSetState { copy(user = it) }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        // Set the available sorting options
        viewModelScope.launchSetState {
            copy(availableSorts = listOf(SortOption.LAST_WATCHED, SortOption.ALPHABETICAL))
        }

        // Subscribe to state changes, so update the observed data source
        subscribe(::updateDataSource)

        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    WatchedAction.RefreshAction -> refresh(fromUser = true)
                    is WatchedAction.FilterShows -> setFilter(action.filter)
                    is WatchedAction.ChangeSort -> setSort(action.sort)
                }
            }
        }

        refresh(false)
    }

    private fun updateDataSource(state: WatchedViewState) {
        observePagedWatchedShows(
            ObservePagedWatchedShows.Params(
                sort = state.sort,
                filter = state.filter,
                pagingConfig = PAGING_CONFIG
            )
        )
    }

    private fun refresh(fromUser: Boolean) {
        viewModelScope.launch {
            if (getTraktAuthState.executeSync(Unit) == TraktAuthState.LOGGED_IN) {
                refreshWatched(fromUser)
            }
        }
    }

    fun submitAction(action: WatchedAction) {
        viewModelScope.launch { pendingActions.emit(action) }
    }

    private fun setFilter(filter: String) {
        viewModelScope.launchSetState {
            copy(filter = filter, filterActive = filter.isNotEmpty())
        }
    }

    private fun setSort(sort: SortOption) {
        viewModelScope.launchSetState { copy(sort = sort) }
    }

    fun clearSelection() {
        showSelection.clearSelection()
    }

    fun onItemClick(show: TiviShow): Boolean {
        return showSelection.onItemClick(show)
    }

    fun onItemLongClick(show: TiviShow): Boolean {
        return showSelection.onItemLongClick(show)
    }

    fun followSelectedShows() {
        viewModelScope.launch {
            changeShowFollowStatus.executeSync(
                ChangeShowFollowStatus.Params(
                    showSelection.getSelectedShowIds(),
                    ChangeShowFollowStatus.Action.FOLLOW,
                    deferDataFetch = true
                )
            )
        }
        showSelection.clearSelection()
    }

    private fun refreshWatched(fromUser: Boolean) {
        viewModelScope.launch {
            updateWatchedShows(UpdateWatchedShows.Params(fromUser))
                .collectInto(loadingState)
        }
    }

    companion object {
        private val PAGING_CONFIG = PagingConfig(
            pageSize = 60,
            prefetchDistance = 20,
            enablePlaceholders = false
        )
    }
}
