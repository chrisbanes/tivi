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

package app.tivi.home.followed

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import app.tivi.AppNavigator
import app.tivi.ReduxViewModel
import app.tivi.data.entities.RefreshType
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.UpdateFollowedShows
import app.tivi.domain.invoke
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObservePagedFollowedShows
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

internal class FollowedViewModel @ViewModelInject constructor(
    private val updateFollowedShows: UpdateFollowedShows,
    private val observePagedFollowedShows: ObservePagedFollowedShows,
    private val observeTraktAuthState: ObserveTraktAuthState,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    private val observeUserDetails: ObserveUserDetails,
    private val appNavigator: AppNavigator
) : ReduxViewModel<FollowedViewState>() {
    private val boundaryCallback = object : PagedList.BoundaryCallback<FollowedShowEntryWithShow>() {
        override fun onZeroItemsLoaded() {
            setState { copy(isEmpty = filter.isNullOrEmpty()) }
        }

        override fun onItemAtEndLoaded(itemAtEnd: FollowedShowEntryWithShow) {
            setState { copy(isEmpty = false) }
        }

        override fun onItemAtFrontLoaded(itemAtFront: FollowedShowEntryWithShow) {
            setState { copy(isEmpty = false) }
        }
    }

    private val loadingState = ObservableLoadingCounter()
    private val showSelection = ShowStateSelector()

    val pagedList: Flow<PagedList<FollowedShowEntryWithShow>>
        get() = observePagedFollowedShows.observe()

    init {
        viewModelScope.launch {
            loadingState.observable
                .distinctUntilChanged()
                .debounce(2000)
                .execute {
                    copy(isLoading = it() ?: false)
                }
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
                    refreshFollowed(false)
                }
            }.execute { copy(authState = it() ?: TraktAuthState.LOGGED_OUT) }
        }
        observeTraktAuthState()

        viewModelScope.launchObserve(observeUserDetails) {
            it.execute { copy(user = it()) }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        // Set the available sorting options
        setState {
            copy(
                availableSorts = listOf(
                    SortOption.SUPER_SORT,
                    SortOption.LAST_WATCHED,
                    SortOption.ALPHABETICAL,
                    SortOption.DATE_ADDED
                )
            )
        }

        // Subscribe to state changes, so update the observed data source
        subscribe(::updateDataSource)

        refresh(false)
    }

    private fun updateDataSource(state: FollowedViewState) {
        observePagedFollowedShows(
            ObservePagedFollowedShows.Parameters(
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
                .also { refreshFollowed(fromUser) }
        }
    }

    fun setFilter(filter: String) {
        setState { copy(filter = filter, filterActive = filter.isNotEmpty()) }
    }

    fun setSort(sort: SortOption) = setState {
        require(availableSorts.contains(sort))
        copy(sort = sort)
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

    fun unfollowSelectedShows() {
        changeShowFollowStatus(
            ChangeShowFollowStatus.Params(
                showSelection.getSelectedShowIds(),
                ChangeShowFollowStatus.Action.UNFOLLOW
            )
        )
        showSelection.clearSelection()
    }

    fun onAccountClicked() {
        appNavigator.openAccount()
    }

    private fun refreshFollowed(fromInteraction: Boolean) {
        updateFollowedShows(UpdateFollowedShows.Params(fromInteraction, RefreshType.QUICK)).also {
            viewModelScope.launch {
                loadingState.collectFrom(it)
            }
        }
    }

    override fun createInitialState(): FollowedViewState {
        return FollowedViewState()
    }

    companion object {
        private val PAGING_CONFIG = PagedList.Config.Builder()
            .setPageSize(60)
            .setPrefetchDistance(20)
            .setEnablePlaceholders(false)
            .build()
    }
}
