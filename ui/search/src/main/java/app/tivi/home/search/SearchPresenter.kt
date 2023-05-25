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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import app.tivi.api.UiMessage
import app.tivi.api.UiMessageManager
import app.tivi.domain.interactors.SearchShows
import app.tivi.screens.SearchScreen
import app.tivi.screens.ShowDetailsScreen
import app.tivi.util.ObservableLoadingCounter
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class SearchUiPresenterFactory(
    private val presenterFactory: (Navigator) -> SearchPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? = when (screen) {
        is SearchScreen -> presenterFactory(navigator)
        else -> null
    }
}

@Inject
class SearchPresenter(
    @Assisted private val navigator: Navigator,
    private val searchShows: SearchShows,
) : Presenter<SearchUiState> {

    @Composable
    override fun present(): SearchUiState {
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
                is SearchUiEvent.OpenShowDetails -> {
                    navigator.goTo(ShowDetailsScreen(event.showId))
                }
            }
        }

        return SearchUiState(
            query = query,
            searchResults = results,
            refreshing = loading,
            message = message,
            eventSink = ::eventSink,
        )
    }
}
