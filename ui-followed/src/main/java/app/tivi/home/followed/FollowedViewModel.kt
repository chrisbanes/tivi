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

import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import app.tivi.TiviMvRxViewModel
import app.tivi.data.entities.RefreshType
import app.tivi.data.entities.SortOption
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.interactors.ObservePagedFollowedShows
import app.tivi.interactors.UpdateFollowedShows
import app.tivi.interactors.launchInteractor
import app.tivi.interactors.launchObserve
import app.tivi.tmdb.TmdbManager
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.ObservableLoadingCounter
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FollowedViewModel @AssistedInject constructor(
    @Assisted initialState: FollowedViewState,
    private val updateFollowedShows: UpdateFollowedShows,
    private val observePagedFollowedShows: ObservePagedFollowedShows,
    private val traktManager: TraktManager,
    tmdbManager: TmdbManager
) : TiviMvRxViewModel<FollowedViewState>(initialState) {
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
            tmdbManager.imageProviderFlow.execute {
                copy(tmdbImageUrlProvider = it() ?: tmdbImageUrlProvider)
            }
        }

        viewModelScope.launchObserve(observePagedFollowedShows) {
            it.execute {
                copy(followedShows = it())
            }
        }

        // Set the available sorting options
        setState {
            copy(availableSorts = listOf(
                    SortOption.SUPER_SORT,
                    SortOption.LAST_WATCHED,
                    SortOption.ALPHABETICAL,
                    SortOption.DATE_ADDED
            ))
        }

        // Subscribe to state changes, so update the observed data source
        subscribe(::updateDataSource)

        refresh()
    }

    private fun updateDataSource(state: FollowedViewState) {
        viewModelScope.launchInteractor(observePagedFollowedShows,
                ObservePagedFollowedShows.Parameters(
                        sort = state.sort,
                        filter = state.filter,
                        pagingConfig = PAGING_CONFIG,
                        boundaryCallback = boundaryCallback
                )
        )
    }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            traktManager.state.first { it == TraktAuthState.LOGGED_IN }
                    .run {
                        refreshFollowed(force)
                    }
        }
    }

    fun setFilter(filter: String) {
        setState { copy(filter = filter, filterActive = filter.isNotEmpty()) }
    }

    fun setSort(sort: SortOption) = setState {
        require(availableSorts.contains(sort))
        copy(sort = sort)
    }

    private fun refreshFollowed(fromUserInteraction: Boolean) {
        loadingState.addLoader()
        viewModelScope.launchInteractor(updateFollowedShows,
                UpdateFollowedShows.Params(fromUserInteraction, RefreshType.QUICK))
                .invokeOnCompletion { loadingState.removeLoader() }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: FollowedViewState): FollowedViewModel
    }

    companion object : MvRxViewModelFactory<FollowedViewModel, FollowedViewState> {
        private val PAGING_CONFIG = PagedList.Config.Builder()
                .setPageSize(60)
                .setPrefetchDistance(20)
                .setEnablePlaceholders(false)
                .build()

        override fun create(viewModelContext: ViewModelContext, state: FollowedViewState): FollowedViewModel? {
            val fragment: FollowedFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.followedViewModelFactory.create(state)
        }
    }
}
