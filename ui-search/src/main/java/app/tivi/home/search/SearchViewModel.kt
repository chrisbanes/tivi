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

import androidx.lifecycle.viewModelScope
import app.tivi.TiviMvRxViewModel
import app.tivi.domain.interactors.SearchShows
import app.tivi.domain.launchObserve
import app.tivi.util.ObservableLoadingCounter
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SearchViewModel @AssistedInject constructor(
    @Assisted initialState: SearchViewState,
    private val searchShows: SearchShows
) : TiviMvRxViewModel<SearchViewState>(initialState) {
    private val searchQuery = ConflatedBroadcastChannel<String>()
    private val loadingState = ObservableLoadingCounter()

    init {
        viewModelScope.launch {
            searchQuery.asFlow()
                    .debounce(300)
                    .collectLatest { query ->
                        loadingState.addLoader()
                        val job = async(searchShows.dispatcher) {
                            searchShows(SearchShows.Params(query))
                        }
                        job.invokeOnCompletion { loadingState.removeLoader() }
                        job.await()
                    }
        }

        viewModelScope.launch {
            loadingState.observable.collect { setState { copy(refreshing = it) } }
        }

        viewModelScope.launchObserve(searchShows) {
            it.execute { copy(searchResults = it()) }
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.sendBlocking(query)
    }

    fun clearQuery() = setSearchQuery("")

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: SearchViewState): SearchViewModel
    }

    companion object : MvRxViewModelFactory<SearchViewModel, SearchViewState> {
        override fun create(viewModelContext: ViewModelContext, state: SearchViewState): SearchViewModel? {
            val fragment: SearchFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.searchViewModelFactory.create(state)
        }
    }
}
