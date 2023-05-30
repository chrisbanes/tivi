// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.recommended

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tivi.common.compose.EntryGrid
import app.tivi.common.ui.resources.MR
import app.tivi.screens.RecommendedShowsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import dev.icerock.moko.resources.compose.stringResource
import me.tatarka.inject.annotations.Inject

@Inject
class RecommendedShowsUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is RecommendedShowsScreen -> {
            ui<RecommendedShowsUiState> { state, modifier ->
                RecommendedShows(state, modifier)
            }
        }

        else -> null
    }
}

@Composable
internal fun RecommendedShows(
    state: RecommendedShowsUiState,
    modifier: Modifier = Modifier,
) {
    // Need to extract the eventSink out to a local val, so that the Compose Compiler
    // treats it as stable. See: https://issuetracker.google.com/issues/256100927
    val eventSink = state.eventSink

    EntryGrid(
        lazyPagingItems = state.items,
        title = stringResource(MR.strings.discover_recommended_title),
        onOpenShowDetails = { eventSink(RecommendedShowsUiEvent.OpenShowDetails(it)) },
        onNavigateUp = { eventSink(RecommendedShowsUiEvent.NavigateUp) },
        modifier = modifier,
    )
}
