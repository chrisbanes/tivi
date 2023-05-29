// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import app.tivi.api.UiMessage
import app.tivi.api.UiMessageManager
import app.tivi.domain.interactors.SearchShows
import app.tivi.util.ObservableLoadingCounter
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class SearchViewModel(
    private val searchShows: SearchShows,
) : ViewModel() {

    @Composable
    fun presenter(): SearchViewState {
        val scope = rememberCoroutineScope()

        var query by remember { mutableStateOf("") }
        val loadingState = remember { ObservableLoadingCounter() }
        val uiMessageManager = remember { UiMessageManager() }

        val loading by loadingState.observable.collectAsState(false)
        val message by uiMessageManager.message.collectAsState(null)
        val results by searchShows.flow.collectAsState(emptyList())

        LaunchedEffect(Unit) {
            snapshotFlow { query }
                .debounce(300)
                .onEach { query ->
                    launch {
                        loadingState.addLoader()
                        searchShows(SearchShows.Params(query))
                    }.invokeOnCompletion {
                        loadingState.removeLoader()
                    }
                }
                .catch { throwable ->
                    uiMessageManager.emitMessage(UiMessage(throwable))
                }
                .collect()
        }

        fun eventSink(event: SearchUiEvent) {
            when (event) {
                is SearchUiEvent.ClearMessage -> {
                    scope.launch {
                        uiMessageManager.clearMessage(event.id)
                    }
                }
                is SearchUiEvent.UpdateQuery -> query = event.query
            }
        }

        return SearchViewState(
            query = query,
            searchResults = results,
            refreshing = loading,
            message = message,
            eventSink = ::eventSink,
        )
    }
}
