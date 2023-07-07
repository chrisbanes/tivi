// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.ProvidedValues
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.providedValuesForBackStack
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.LocalCircuitConfig
import com.slack.circuit.foundation.screen
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen

@Composable
expect fun TiviNavigableCircuitContent(
    navigator: Navigator,
    backstack: SaveableBackStack,
    modifier: Modifier = Modifier,
    circuitConfig: CircuitConfig = requireNotNull(LocalCircuitConfig.current),
    providedValues: Map<out BackStack.Record, ProvidedValues> = providedValuesForBackStack(backstack),
    // decoration: NavDecoration = circuitConfig.defaultNavDecoration,
    unavailableRoute: (@Composable (screen: Screen, modifier: Modifier) -> Unit) =
        circuitConfig.onUnavailableContent,
)

@Stable
internal class ContentProvider(
    val backStackRecord: SaveableBackStack.Record,
    val content: @Composable () -> Unit,
) {
    var wasSwiped: Boolean by mutableStateOf(false)
}

@Composable
internal fun buildCircuitContentProvider(
    navigator: Navigator,
    backstack: SaveableBackStack,
    circuitConfig: CircuitConfig,
    unavailableRoute: @Composable (screen: Screen, modifier: Modifier) -> Unit,
): List<ContentProvider> = buildList {
    for (record in backstack) {
        val provider = key(record.key) {
            val currentContent: (@Composable (SaveableBackStack.Record) -> Unit) = {
                @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                com.slack.circuit.foundation.CircuitContent(
                    screen = record.screen,
                    modifier = Modifier,
                    navigator = navigator,
                    circuitConfig = circuitConfig,
                    unavailableContent = unavailableRoute,
                )
            }

            val currentRouteContent by rememberUpdatedState(currentContent)
            val currentRecord by rememberUpdatedState(record)
            remember { movableContentOf { currentRouteContent(currentRecord) } }
        }

        add(ContentProvider(record, provider))
    }
}
