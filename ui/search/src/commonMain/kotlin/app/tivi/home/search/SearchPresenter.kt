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
import app.tivi.common.compose.UiMessage
import app.tivi.common.compose.UiMessageManager
import app.tivi.data.models.TiviShow
import app.tivi.domain.interactors.SearchShows
import app.tivi.screens.SearchScreen
import app.tivi.screens.ShowDetailsScreen
import app.tivi.util.Logger
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
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
  private val searchShows: Lazy<SearchShows>,
  private val logger: Logger,
) : Presenter<SearchUiState> {

  @Composable
  override fun present(): SearchUiState {
    val scope = rememberCoroutineScope()

    var query by rememberRetained { mutableStateOf("") }
    var results by rememberRetained { mutableStateOf(emptyList<TiviShow>()) }

    val uiMessageManager = remember { UiMessageManager() }

    val loading by searchShows.value.inProgress.collectAsState(false)
    val message by uiMessageManager.message.collectAsState(null)

    LaunchedEffect(query) {
      // delay for 300 milliseconds. This has the same effect as debounce
      delay(300.milliseconds)

      val result = searchShows.value.invoke(SearchShows.Params(query))
      results = result.getOrDefault(emptyList())

      result.onFailure { e ->
        logger.i(e)
        uiMessageManager.emitMessage(UiMessage(e))
      }
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
