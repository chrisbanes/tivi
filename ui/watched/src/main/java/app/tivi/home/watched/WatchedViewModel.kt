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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.tivi.api.UiMessageManager
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.GetTraktAuthState
import app.tivi.domain.interactors.UpdateWatchedShows
import app.tivi.domain.observers.ObservePagedWatchedShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.extensions.combine
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.ShowStateSelector
import app.tivi.util.collectStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WatchedViewModel @Inject constructor(
    private val updateWatchedShows: UpdateWatchedShows,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    private val observePagedWatchedShows: ObservePagedWatchedShows,
    observeTraktAuthState: ObserveTraktAuthState,
    private val getTraktAuthState: GetTraktAuthState,
    observeUserDetails: ObserveUserDetails,
    private val logger: Logger
) : ViewModel() {
    private val uiMessageManager = UiMessageManager()

    private val availableSorts = listOf(SortOption.LAST_WATCHED, SortOption.ALPHABETICAL)

    private val loadingState = ObservableLoadingCounter()
    private val showSelection = ShowStateSelector()

    val pagedList: Flow<PagingData<WatchedShowEntryWithShow>> =
        observePagedWatchedShows.flow.cachedIn(viewModelScope)

    private val filter = MutableStateFlow<String?>(null)
    private val sort = MutableStateFlow(SortOption.LAST_WATCHED)

    val state: StateFlow<WatchedViewState> = combine(
        loadingState.observable,
        showSelection.observeSelectedShowIds(),
        showSelection.observeIsSelectionOpen(),
        observeTraktAuthState.flow,
        observeUserDetails.flow,
        filter,
        sort,
        uiMessageManager.message
    ) { loading, selectedShowIds, isSelectionOpen, authState, user, filter, sort, message ->
        WatchedViewState(
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
        started = SharingStarted.WhileSubscribed(),
        initialValue = WatchedViewState.Empty
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
        observePagedWatchedShows(
            ObservePagedWatchedShows.Params(
                sort = sort.value,
                filter = filter.value,
                pagingConfig = PAGING_CONFIG
            )
        )
    }

    fun refresh(fromUser: Boolean = true) {
        viewModelScope.launch {
            if (getTraktAuthState.executeSync(Unit) == TraktAuthState.LOGGED_IN) {
                refreshWatched(fromUser)
            }
        }
    }

    fun setFilter(filter: String?) {
        viewModelScope.launch {
            this@WatchedViewModel.filter.emit(filter)
        }
    }

    fun setSort(sort: SortOption) {
        viewModelScope.launch {
            this@WatchedViewModel.sort.emit(sort)
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
            updateWatchedShows(
                UpdateWatchedShows.Params(forceRefresh = fromUser)
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
