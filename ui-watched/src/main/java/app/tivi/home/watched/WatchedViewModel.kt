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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import app.tivi.AppNavigator
import app.tivi.ReduxViewModel
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.UpdateWatchedShows
import app.tivi.domain.invoke
import app.tivi.domain.observers.ObservePagedWatchedShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.ShowStateSelector
import app.tivi.util.collectInto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class WatchedViewModel @ViewModelInject constructor(
    private val updateWatchedShows: UpdateWatchedShows,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    private val observePagedWatchedShows: ObservePagedWatchedShows,
    private val observeTraktAuthState: ObserveTraktAuthState,
    observeUserDetails: ObserveUserDetails,
    private val appNavigator: AppNavigator
) : ReduxViewModel<WatchedViewState>(
    WatchedViewState()
) {
    private val boundaryCallback = object : PagedList.BoundaryCallback<WatchedShowEntryWithShow>() {
        override fun onZeroItemsLoaded() {
            viewModelScope.launchSetState { copy(isEmpty = filter.isNullOrEmpty()) }
        }

        override fun onItemAtEndLoaded(itemAtEnd: WatchedShowEntryWithShow) {
            viewModelScope.launchSetState { copy(isEmpty = false) }
        }

        override fun onItemAtFrontLoaded(itemAtFront: WatchedShowEntryWithShow) {
            viewModelScope.launchSetState { copy(isEmpty = false) }
        }
    }

    private val loadingState = ObservableLoadingCounter()
    private val showSelection = ShowStateSelector()

    val pagedList: Flow<PagedList<WatchedShowEntryWithShow>>
        get() = observePagedWatchedShows.observe()

    init {
        viewModelScope.launch {
            loadingState.observable
                .distinctUntilChanged()
                .debounce(2000)
                .collectAndSetState { copy(isLoading = it) }
        }

        viewModelScope.launch {
            showSelection.observeSelectedShowIds().collectAndSetState { copy(selectedShowIds = it) }
        }

        viewModelScope.launch {
            showSelection.observeIsSelectionOpen().collectAndSetState { copy(selectionOpen = it) }
        }

        viewModelScope.launch {
            observeTraktAuthState.observe()
                .distinctUntilChanged()
                .onEach { if (it == TraktAuthState.LOGGED_IN) refresh(false) }
                .collectAndSetState { copy(authState = it) }
        }
        observeTraktAuthState()

        viewModelScope.launch {
            observeUserDetails.observe().collectAndSetState { copy(user = it) }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        // Set the available sorting options
        viewModelScope.launchSetState {
            copy(availableSorts = listOf(SortOption.LAST_WATCHED, SortOption.ALPHABETICAL))
        }

        // Subscribe to state changes, so update the observed data source
        subscribe(::updateDataSource)

        refresh(false)
    }

    private fun updateDataSource(state: WatchedViewState) {
        observePagedWatchedShows(
            ObservePagedWatchedShows.Params(
                sort = state.sort,
                filter = state.filter,
                pagingConfig = PAGING_CONFIG,
                boundaryCallback = boundaryCallback
            )
        )
    }

    fun refresh() = refresh(true)

    private fun refresh(fromUser: Boolean) {
        viewModelScope.launch {
            observeTraktAuthState.observe().first().also { authState ->
                if (authState == TraktAuthState.LOGGED_IN) {
                    refreshWatched(fromUser)
                }
            }
        }
    }

    fun setFilter(filter: String) {
        viewModelScope.launchSetState {
            copy(filter = filter, filterActive = filter.isNotEmpty())
        }
    }

    fun setSort(sort: SortOption) {
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
        private val PAGING_CONFIG = PagedList.Config.Builder()
            .setPageSize(60)
            .setPrefetchDistance(20)
            .setEnablePlaceholders(false)
            .build()
    }
}
