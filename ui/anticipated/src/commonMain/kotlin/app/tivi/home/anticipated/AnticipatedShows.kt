// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.anticipated

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tivi.common.compose.EntryGrid
import app.tivi.common.ui.resources.Res
import app.tivi.common.ui.resources.discover_anticipated_title
import app.tivi.screens.AnticipatedShowsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

@Inject
class AnticipatedShowsUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is AnticipatedShowsScreen -> {
      ui<AnticipatedShowsUiState> { state, modifier ->
        AnticipatedShows(state, modifier)
      }
    }

    else -> null
  }
}

@Composable
internal fun AnticipatedShows(
  state: AnticipatedShowsUiState,
  modifier: Modifier = Modifier,
) {
  // Need to extract the eventSink out to a local val, so that the Compose Compiler
  // treats it as stable. See: https://issuetracker.google.com/issues/256100927
  val eventSink = state.eventSink

  EntryGrid(
    lazyPagingItems = state.items,
    title = stringResource(Res.string.discover_anticipated_title),
    onOpenShowDetails = { eventSink(AnticipatedShowsUiEvent.OpenShowDetails(it)) },
    onNavigateUp = { eventSink(AnticipatedShowsUiEvent.NavigateUp) },
    modifier = modifier,
  )
}
