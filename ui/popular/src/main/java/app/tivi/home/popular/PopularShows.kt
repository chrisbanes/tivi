/*
 * Copyright 2023 Google LLC
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
