/*
 * Copyright 2022 Google LLC
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

package app.tivi.home.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.tivi.api.UiMessageManager
import app.tivi.data.compoundmodels.LibraryShow
import app.tivi.data.models.SortOption
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.executeSync
import app.tivi.domain.interactors.ChangeShowFollowStatus
import app.tivi.domain.interactors.GetTraktAuthState
import app.tivi.domain.interactors.UpdateLibraryShows
import app.tivi.domain.observers.ObservePagedLibraryShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.extensions.combine
import app.tivi.settings.TiviPreferences
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class LibraryViewModel(
    private val updateLibraryShows: UpdateLibraryShows,
    private val observePagedLibraryShows: ObservePagedLibraryShows,
    observeTraktAuthState: ObserveTraktAuthState,
    private val changeShowFollowStatus: ChangeShowFollowStatus,
    observeUserDetails: ObserveUserDetails,
    private val getTraktAuthState: GetTraktAuthState,
    private val preferences: TiviPreferences,
    private val logger: Logger,
) : ViewModel() {
    private val followedLoadingState = ObservableLoadingCounter()
    private val watchedLoadingState = ObservableLoadingCounter()
    private val uiMessageManager = UiMessageManager()

    val pagedList: Flow<PagingData<LibraryShow>> =
        observePagedLibraryShows.flow.cachedIn(viewModelScope)

    private val availableSorts = listOf(
        SortOption.LAST_WATCHED,
        SortOption.ALPHABETICAL,
    )

    private val filter = MutableStateFlow<String?>(null)
    private val sort = MutableStateFlow(SortOption.LAST_WATCHED)

    val state: StateFlow<LibraryViewState> = combine(
        followedLoadingState.observable,
        watchedLoadingState.observable,
        observeTraktAuthState.flow,
        observeUserDetails.flow,
        filter,
        sort,
        uiMessageManager.message,
        preferences.observeLibraryWatchedActive(),
        preferences.observeLibraryFollowedActive(),
    ) { followedLoading, watchedLoading, authState, user, filter, sort, message, includeWatchedShows, includeFollowedShows ->
        LibraryViewState(
            user = user,
            authState = authState,
            isLoading = followedLoading || watchedLoading,
            filter = filter,
            filterActive = !filter.isNullOrEmpty(),
            availableSorts = availableSorts,
            sort = sort,
            message = message,
            watchedShowsIncluded = includeWatchedShows,
            followedShowsIncluded = includeFollowedShows,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(),
        initialValue = LibraryViewState.Empty,
    )

    init {
        observeTraktAuthState(Unit)
        observeUserDetails(ObserveUserDetails.Params("me"))

        // When the filter and sort options change, update the data source
        filter
            .onEach { updateDataSource() }
            .launchIn(viewModelScope)

        sort
            .onEach { updateDataSource() }
            .launchIn(viewModelScope)

        preferences.observeLibraryWatchedActive()
            .onEach { updateDataSource() }
            .launchIn(viewModelScope)

        preferences.observeLibraryFollowedActive()
            .onEach { updateDataSource() }
            .launchIn(viewModelScope)

        // When the user logs in, refresh...
        observeTraktAuthState.flow
            .filter { it == TraktAuthState.LOGGED_IN }
            .onEach { refresh(false) }
            .launchIn(viewModelScope)
    }

    private fun updateDataSource() {
        observePagedLibraryShows(
            ObservePagedLibraryShows.Parameters(
                sort = sort.value,
                filter = filter.value,
                includeFollowed = preferences.libraryFollowedActive,
                includeWatched = preferences.libraryWatchedActive,
                pagingConfig = PAGING_CONFIG,
            ),
        )
    }

    fun refresh(fromUser: Boolean = true) {
        viewModelScope.launch {
            if (getTraktAuthState.executeSync() == TraktAuthState.LOGGED_IN) {
                refreshFollowed(fromUser)
            }
        }
    }

    fun setFilter(filter: String?) {
        viewModelScope.launch {
            this@LibraryViewModel.filter.emit(filter)
        }
    }

    fun setSort(sort: SortOption) {
        viewModelScope.launch {
            this@LibraryViewModel.sort.emit(sort)
        }
    }

    fun toggleFollowedShowsIncluded() {
        preferences.libraryFollowedActive = !preferences.libraryFollowedActive
    }

    fun toggleWatchedShowsIncluded() {
        preferences.libraryWatchedActive = !preferences.libraryWatchedActive
    }

    private fun refreshFollowed(fromInteraction: Boolean) {
        viewModelScope.launch {
            updateLibraryShows(
                UpdateLibraryShows.Params(fromInteraction),
            ).collectStatus(followedLoadingState, logger, uiMessageManager)
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
            initialLoadSize = 32,
        )
    }
}
