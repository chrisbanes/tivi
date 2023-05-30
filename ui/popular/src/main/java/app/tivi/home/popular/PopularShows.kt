// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.popular

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tivi.common.compose.EntryGrid
import app.tivi.common.ui.resources.MR
import app.tivi.screens.PopularShowsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import dev.icerock.moko.resources.compose.stringResource
import me.tatarka.inject.annotations.Inject

@Inject
class PopularShowsUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is PopularShowsScreen -> {
            ui<PopularShowsUiState> { state, modifier ->
                PopularShows(state, modifier)
            }
        }

        else -> null
    }
}

@Composable
internal fun PopularShows(
    state: PopularShowsUiState,
    modifier: Modifier = Modifier,
) {
    // Need to extract the eventSink out to a local val, so that the Compose Compiler
    // treats it as stable. See: https://issuetracker.google.com/issues/256100927
    val eventSink = state.eventSink

    EntryGrid(
        lazyPagingItems = state.items,
        title = stringResource(MR.strings.discover_popular_title),
        onOpenShowDetails = { eventSink(PopularShowsUiEvent.OpenShowDetails(it)) },
        onNavigateUp = { eventSink(PopularShowsUiEvent.NavigateUp) },
        modifier = modifier,
    )
}
