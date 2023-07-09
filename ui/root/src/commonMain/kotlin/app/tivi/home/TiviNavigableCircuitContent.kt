// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.ProvidedValues
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.providedValuesForBackStack
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.CircuitContent
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

@Immutable
internal data class RecordContentProvider(
    val backStackRecord: SaveableBackStack.Record,
    val content: @Composable (SaveableBackStack.Record) -> Unit,
)

@Composable
internal fun SaveableBackStack.buildCircuitContentProviders(
    navigator: Navigator,
    circuitConfig: CircuitConfig,
    unavailableRoute: @Composable (screen: Screen, modifier: Modifier) -> Unit,
): List<RecordContentProvider> {
    val previousContentProviders = remember { mutableMapOf<String, RecordContentProvider>() }

    val lastNavigator by rememberUpdatedState(navigator)
    val lastCircuitConfig by rememberUpdatedState(circuitConfig)
    val lastUnavailableRoute by rememberUpdatedState(unavailableRoute)

    return iterator()
        .asSequence()
        .map { record ->
            // Query the previous content providers map, so that we use the same
            // RecordContentProvider instances across calls.
            previousContentProviders.getOrElse(record.key) {
                RecordContentProvider(
                    backStackRecord = record,
                    content = movableContentOf { record ->
                        CircuitContent(
                            screen = record.screen,
                            modifier = Modifier,
                            navigator = lastNavigator,
                            circuitConfig = lastCircuitConfig,
                            unavailableContent = lastUnavailableRoute,
                        )
                    },
                )
            }
        }
        .toList()
        .also { list ->
            // Update the previousContentProviders map so we can reference it on the next call
            previousContentProviders.clear()
            for (provider in list) {
                previousContentProviders[provider.backStackRecord.key] = provider
            }
        }
}
