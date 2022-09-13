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
import androidx.paging.cachedIn
import app.tivi.api.UiMessageManager
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
import app.tivi.extensions.combine
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.ShowStateSelector
import app.tivi.util.collectStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FollowedViewModel @Inject constructor(
    private val updateFollowedShows: UpdateFollowedShows,
    private val observePagedFollowedShows: ObservePagedFollowedShows,
    private val observeTraktAuthState: ObserveTraktAuthState,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    observeUserDetails: ObserveUserDetails,
    private val getTraktAuthState: GetTraktAuthState,
    private val logger: Logger
) : ViewModel() {
    private val loadingState = ObservableLoadingCounter()
    private val uiMessageManager = UiMessageManager()
    private val showSelection = ShowStateSelector()

    val pagedList: Flow<PagingData<FollowedShowEntryWithShow>> =
        observePagedFollowedShows.flow.cachedIn(viewModelScope)

    private val availableSorts = listOf(
        SortOption.SUPER_SORT,
        SortOption.LAST_WATCHED,
        SortOption.ALPHABETICAL,
        SortOption.DATE_ADDED
    )

    private val filter = MutableStateFlow<String?>(null)
    private val sort = MutableStateFlow(SortOption.SUPER_SORT)

    val state: StateFlow<FollowedViewState> = combine(
        loadingState.observable,
        showSelection.observeSelectedShowIds(),
        showSelection.observeIsSelectionOpen(),
        observeTraktAuthState.flow,
        observeUserDetails.flow,
        filter,
        sort,
        uiMessageManager.message
    ) { loading, selectedShowIds, isSelectionOpen, authState, user, filter, sort, message ->
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
            message = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = FollowedViewState.Empty
    )

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
            // When the user logs in, refresh...
            observeTraktAuthState.flow
                .filter { it == TraktAuthState.LOGGED_IN }
                .collect { refresh(false) }
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

    fun refresh(fromUser: Boolean = true) {
        viewModelScope.launch {
            if (getTraktAuthState.executeSync(Unit) == TraktAuthState.LOGGED_IN) {
                refreshFollowed(fromUser)
            }
        }
    }

    fun setFilter(filter: String?) {
        viewModelScope.launch {
            this@FollowedViewModel.filter.emit(filter)
        }
    }

    fun setSort(sort: SortOption) {
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
            updateFollowedShows(
                UpdateFollowedShows.Params(fromInteraction, RefreshType.QUICK)
            ).collectStatus(loadingState, logger, uiMessageManager)
        }
    }

    fun clearMessage(id: Long) {
        viewModelScope.launch {
            uiMessageManager.clearMessage(id)
        }
    }

    companion object {
        private val PAGING_CONFIG = PagingConfig(
            pageSize = 16,
            initialLoadSize = 32
        )
    }
}
