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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.common.compose.combine
import app.tivi.data.entities.RefreshType
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.GetTraktAuthState
import app.tivi.domain.interactors.UpdateFollowedShows
import app.tivi.domain.observers.ObservePagedFollowedShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.ShowStateSelector
import app.tivi.util.collectInto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FollowedViewModel @Inject constructor(
    private val updateFollowedShows: UpdateFollowedShows,
    private val observePagedFollowedShows: ObservePagedFollowedShows,
    private val observeTraktAuthState: ObserveTraktAuthState,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    private val observeUserDetails: ObserveUserDetails,
    private val getTraktAuthState: GetTraktAuthState,
) : ViewModel() {
    private val pendingActions = MutableSharedFlow<FollowedAction>()

    private val loadingState = ObservableLoadingCounter()
    private val showSelection = ShowStateSelector()

    val pagedList: Flow<PagingData<FollowedShowEntryWithShow>>
        get() = observePagedFollowedShows.observe()

    private val availableSorts = listOf(
        SortOption.SUPER_SORT,
        SortOption.LAST_WATCHED,
        SortOption.ALPHABETICAL,
        SortOption.DATE_ADDED
    )

    private val filter = MutableStateFlow<String?>(null)
    private val sort = MutableStateFlow(SortOption.SUPER_SORT)

    val state: Flow<FollowedViewState> = combine(
        loadingState.observable.debounce(2000),
        showSelection.observeSelectedShowIds(),
        showSelection.observeIsSelectionOpen(),
        observeTraktAuthState.observe()
            .onEach { if (it == TraktAuthState.LOGGED_IN) refresh(false) },
        observeUserDetails.observe(),
        filter,
        sort,
    ) { loading, selectedShowIds, isSelectionOpen, authState, user, filter, sort ->
        FollowedViewState(
            user = user,
            authState = authState,
            isLoading = loading,
            selectionOpen = isSelectionOpen,
            selectedShowIds = selectedShowIds,
            filter = filter,
            filterActive = !filter.isNullOrEmpty(),
            availableSorts = availableSorts,
            sort = sort,
        )
    }

    init {
        observeTraktAuthState(Unit)
        observeUserDetails(ObserveUserDetails.Params("me"))

        // When the filter and sort options change, update the data source
        viewModelScope.launch {
            filter.collect { updateDataSource() }
        }
        viewModelScope.launch {
            sort.collect { updateDataSource() }
        }

        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    FollowedAction.RefreshAction -> refresh(true)
                    is FollowedAction.FilterShows -> setFilter(action.filter)
                    is FollowedAction.ChangeSort -> setSort(action.sort)
                }
            }
        }
    }

    private fun updateDataSource() {
        observePagedFollowedShows(
            ObservePagedFollowedShows.Parameters(
                sort = sort.value,
                filter = filter.value,
                pagingConfig = PAGING_CONFIG
            )
        )
    }

    private fun refresh(fromUser: Boolean) {
        viewModelScope.launch {
            if (getTraktAuthState.executeSync(Unit) == TraktAuthState.LOGGED_IN) {
                refreshFollowed(fromUser)
            }
        }
    }

    private fun setFilter(filter: String?) {
        viewModelScope.launch {
            this@FollowedViewModel.filter.emit(filter)
        }
    }

    private fun setSort(sort: SortOption) {
        viewModelScope.launch {
            this@FollowedViewModel.sort.emit(sort)
        }
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
        viewModelScope.launch {
            changeShowFollowStatus.executeSync(
                ChangeShowFollowStatus.Params(
                    showSelection.getSelectedShowIds(),
                    ChangeShowFollowStatus.Action.UNFOLLOW
                )
            )
        }
        showSelection.clearSelection()
    }

    private fun refreshFollowed(fromInteraction: Boolean) {
        viewModelScope.launch {
            updateFollowedShows(UpdateFollowedShows.Params(fromInteraction, RefreshType.QUICK))
                .collectInto(loadingState)
        }
    }

    fun submitAction(action: FollowedAction) {
        viewModelScope.launch {
            pendingActions.emit(action)
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
