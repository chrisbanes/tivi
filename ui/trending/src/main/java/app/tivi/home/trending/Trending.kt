/*
 * Copyright 2020 Google LLC
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

package app.tivi.home.trending

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tivi.common.compose.EntryGrid
import app.tivi.common.ui.resources.MR
import app.tivi.screens.TrendingShowsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import dev.icerock.moko.resources.compose.stringResource
import me.tatarka.inject.annotations.Inject

@Inject
class TrendingShowsUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is TrendingShowsScreen -> {
            ui<TrendingShowsUiState> { state, modifier ->
                TrendingShows(state, modifier)
            }
        }

        else -> null
    }
}

@Composable
internal fun TrendingShows(
    state: TrendingShowsUiState,
    modifier: Modifier = Modifier,
) {
    EntryGrid(
        lazyPagingItems = state.items,
        title = stringResource(MR.strings.discover_trending_title),
        onOpenShowDetails = { state.eventSink(TrendingShowsUiEvent.OpenShowDetails(it)) },
        onNavigateUp = { state.eventSink(TrendingShowsUiEvent.NavigateUp) },
        modifier = modifier,
    )
}
