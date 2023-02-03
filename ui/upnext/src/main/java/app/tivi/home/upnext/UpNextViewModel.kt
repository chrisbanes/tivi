/*
 * Copyright 2023 Google LLC
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

package app.tivi.home.upnext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.tivi.api.UiMessageManager
import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.models.SortOption
import app.tivi.domain.executeSync
import app.tivi.domain.interactors.GetTraktAuthState
import app.tivi.domain.interactors.UpdateUpNextEpisodes
import app.tivi.domain.observers.ObservePagedUpNextShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
internal class UpNextViewModel @Inject constructor(
    private val observePagedUpNextShows: ObservePagedUpNextShows,
    private val updateUpNextEpisodes: UpdateUpNextEpisodes,
    observeTraktAuthState: ObserveTraktAuthState,
    observeUserDetails: ObserveUserDetails,
    private val getTraktAuthState: GetTraktAuthState,
    private val logger: Logger,
) : ViewModel() {
    private val loadingState = ObservableLoadingCounter()
    private val uiMessageManager = UiMessageManager()

    val pagedList: Flow<PagingData<UpNextEntry>> =
        observePagedUpNextShows.flow.cachedIn(viewModelScope)

    private val availableSorts = listOf(
        SortOption.LAST_WATCHED,
        SortOption.AIR_DATE,
        SortOption.DATE_ADDED,
    )

    private val sort = MutableStateFlow(SortOption.LAST_WATCHED)

    val state: StateFlow<UpNextViewState> = combine(
        loadingState.observable,
        observeTraktAuthState.flow,
        observeUserDetails.flow,
        sort,
        uiMessageManager.message,
    ) { loading, authState, user, sort, message ->
        UpNextViewState(
            user = user,
            authState = authState,
            isLoading = loading,
            availableSorts = availableSorts,
            sort = sort,
            message = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(),
        initialValue = UpNextViewState.Empty,
    )

    init {
        observeTraktAuthState(Unit)
        observeUserDetails(ObserveUserDetails.Params("me"))

        // When the sort options change, update the data source
        sort
            .onEach { updateDataSource() }
            .launchIn(viewModelScope)

        // When the user logs in, refresh...
        observeTraktAuthState.flow
            .filter { it == TraktAuthState.LOGGED_IN }
            .onEach { refresh(false) }
            .launchIn(viewModelScope)
    }

    private fun updateDataSource() {
        observePagedUpNextShows(
            ObservePagedUpNextShows.Parameters(
                sort = sort.value,
                pagingConfig = PAGING_CONFIG,
            ),
        )
    }

    fun refresh(fromUser: Boolean = true) {
        viewModelScope.launch {
            if (getTraktAuthState.executeSync() == TraktAuthState.LOGGED_IN) {
                updateUpNextEpisodes(
                    UpdateUpNextEpisodes.Params(forceRefresh = fromUser),
                ).collectStatus(loadingState, logger, uiMessageManager)
            }
        }
    }

    fun setSort(sort: SortOption) {
        viewModelScope.launch {
            this@UpNextViewModel.sort.emit(sort)
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
