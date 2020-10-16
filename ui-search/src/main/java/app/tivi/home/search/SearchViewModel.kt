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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import app.tivi.ReduxViewModel
import app.tivi.domain.interactors.SearchShows
import app.tivi.util.ObservableLoadingCounter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

internal class SearchViewModel @ViewModelInject constructor(
    private val searchShows: SearchShows
) : ReduxViewModel<SearchViewState>(
    SearchViewState()
) {
    private val searchQuery = MutableStateFlow("")
    private val loadingState = ObservableLoadingCounter()

    private val pendingActions = Channel<SearchAction>(Channel.BUFFERED)

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
            loadingState.observable.collectAndSetState { copy(refreshing = it) }
        }

        viewModelScope.launch {
            searchShows.observe().collectAndSetState { copy(searchResults = it) }
        }

        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is SearchAction.Search -> {
                        searchQuery.value = action.searchTerm
                    }
                }
            }
        }
    }

    fun submitAction(action: SearchAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                pendingActions.send(action)
            }
        }
    }
}
