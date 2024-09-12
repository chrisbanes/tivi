// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.tivi.common.compose.UiMessage
import app.tivi.common.compose.UiMessageManager
import app.tivi.data.models.TiviShow
import app.tivi.domain.interactors.SearchShows
import app.tivi.screens.SearchScreen
import app.tivi.screens.ShowDetailsScreen
import app.tivi.util.launchOrThrow
import app.tivi.wrapEventSink
import co.touchlab.kermit.Logger
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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
) : Presenter<SearchUiState> {

  @Composable
  override fun present(): SearchUiState {
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
        Logger.i(e) { "Error whilst calling SearchShows" }
        uiMessageManager.emitMessage(UiMessage(e))
      }
    }

    val eventSink: CoroutineScope.(SearchUiEvent) -> Unit = { event ->
      when (event) {
        is SearchUiEvent.ClearMessage -> {
          launchOrThrow {
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
      eventSink = wrapEventSink(eventSink),
    )
  }
}
