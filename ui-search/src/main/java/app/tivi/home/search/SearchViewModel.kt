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

package app.tivi.home.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tivi.domain.interactors.SearchShows
import app.tivi.util.ObservableLoadingCounter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val searchShows: SearchShows
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val loadingState = ObservableLoadingCounter()

    private val pendingActions = MutableSharedFlow<SearchAction>()

    val state: StateFlow<SearchViewState> = combine(
        searchShows.flow,
        loadingState.observable,
    ) { results, refreshing ->
        SearchViewState(
            searchResults = results,
            refreshing = refreshing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchViewState.Empty,
    )

    init {
        viewModelScope.launch {
            searchQuery.debounce(300)
                .collectLatest { query ->
                    val job = launch {
                        loadingState.addLoader()
                        searchShows(SearchShows.Params(query))
                    }
                    job.invokeOnCompletion { loadingState.removeLoader() }
                    job.join()
                }
        }

        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    is SearchAction.Search -> {
                        searchQuery.value = action.searchTerm
                    }
                    else -> Unit
                }
            }
        }
    }

    fun submitAction(action: SearchAction) {
        viewModelScope.launch {
            pendingActions.emit(action)
        }
    }
}
