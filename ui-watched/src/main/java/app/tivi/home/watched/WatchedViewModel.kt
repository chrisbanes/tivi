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
import androidx.paging.PagedList
import app.tivi.AppNavigator
import app.tivi.ReduxViewModel
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.UpdateWatchedShows
import app.tivi.domain.invoke
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObservePagedWatchedShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.ShowStateSelector
import app.tivi.util.collectFrom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class WatchedViewModel @Inject constructor(
    private val updateWatchedShows: UpdateWatchedShows,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    private val observePagedWatchedShows: ObservePagedWatchedShows,
    private val observeTraktAuthState: ObserveTraktAuthState,
    observeUserDetails: ObserveUserDetails,
    private val appNavigator: AppNavigator
) : ReduxViewModel<WatchedViewState>() {
    private val boundaryCallback = object : PagedList.BoundaryCallback<WatchedShowEntryWithShow>() {
        override fun onZeroItemsLoaded() {
            setState { copy(isEmpty = filter.isNullOrEmpty()) }
        }

        override fun onItemAtEndLoaded(itemAtEnd: WatchedShowEntryWithShow) {
            setState { copy(isEmpty = false) }
        }

        override fun onItemAtFrontLoaded(itemAtFront: WatchedShowEntryWithShow) {
            setState { copy(isEmpty = false) }
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
                .execute { copy(isLoading = it() ?: false) }
        }

        viewModelScope.launch {
            showSelection.observeSelectedShowIds().collect {
                setState { copy(selectedShowIds = it) }
            }
        }

        viewModelScope.launch {
            showSelection.observeIsSelectionOpen().collect {
                setState { copy(selectionOpen = it) }
            }
        }

        viewModelScope.launchObserve(observeTraktAuthState) { flow ->
            flow.distinctUntilChanged().onEach {
                if (it == TraktAuthState.LOGGED_IN) {
                    refresh(false)
                }
            }.execute {
                copy(authState = it() ?: TraktAuthState.LOGGED_OUT)
            }
        }
        observeTraktAuthState()

        viewModelScope.launchObserve(observeUserDetails) {
            it.execute { copy(user = it()) }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        // Set the available sorting options
        setState {
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
            observeTraktAuthState.observe()
                .first { it == TraktAuthState.LOGGED_IN }
                .also { refreshWatched(fromUser) }
        }
    }

    fun onAccountClicked() {
        appNavigator.openAccount()
    }

    fun setFilter(filter: String) {
        setState { copy(filter = filter, filterActive = filter.isNotEmpty()) }
    }

    fun setSort(sort: SortOption) {
        setState { copy(sort = sort) }
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
        changeShowFollowStatus(
            ChangeShowFollowStatus.Params(
                showSelection.getSelectedShowIds(),
                ChangeShowFollowStatus.Action.FOLLOW,
                deferDataFetch = true
            )
        )
        showSelection.clearSelection()
    }

    fun unfollowSelectedShows() {
        changeShowFollowStatus(
            ChangeShowFollowStatus.Params(
                showSelection.getSelectedShowIds(),
                ChangeShowFollowStatus.Action.UNFOLLOW
            )
        )
        showSelection.clearSelection()
    }

    private fun refreshWatched(fromUser: Boolean) {
        updateWatchedShows(UpdateWatchedShows.Params(fromUser)).also {
            viewModelScope.launch {
                loadingState.collectFrom(it)
            }
        }
    }

    override fun createInitialState(): WatchedViewState {
        return WatchedViewState()
    }

    companion object {
        private val PAGING_CONFIG = PagedList.Config.Builder()
            .setPageSize(60)
            .setPrefetchDistance(20)
            .setEnablePlaceholders(false)
            .build()
    }
}
