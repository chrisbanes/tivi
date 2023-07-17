// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
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

@Stable
interface NavDecorationWithPrevious {
    @Composable
    fun <T> DecoratedContent(
        arg: T,
        previous: T?,
        backStackDepth: Int,
        modifier: Modifier,
        content: @Composable (T) -> Unit,
    )
}

@Composable
fun NavigableCircuitContentWithPrevious(
    navigator: Navigator,
    backstack: SaveableBackStack,
    modifier: Modifier = Modifier,
    circuitConfig: CircuitConfig = requireNotNull(LocalCircuitConfig.current),
    providedValues: Map<out BackStack.Record, ProvidedValues> = providedValuesForBackStack(backstack),
    decoration: NavDecorationWithPrevious,
    unavailableRoute: (@Composable (screen: Screen, modifier: Modifier) -> Unit) =
        circuitConfig.onUnavailableContent,
) {
    val activeContentProviders = backstack.buildCircuitContentProviders(
        navigator = navigator,
        circuitConfig = circuitConfig,
        unavailableRoute = unavailableRoute,
    )

    if (backstack.size > 0) {
        @Suppress("SpreadOperator")
        decoration.DecoratedContent(
            arg = activeContentProviders.first(),
            previous = activeContentProviders.getOrNull(1),
            backStackDepth = backstack.size,
            modifier = modifier,
        ) { contentProvider ->
            val values = providedValues[contentProvider.backStackRecord]?.provideValues()
            val providedLocals = remember(values) { values?.toTypedArray() ?: emptyArray() }
            CompositionLocalProvider(*providedLocals) {
                contentProvider.content(contentProvider.backStackRecord)
            }
        }
    }
}

@Immutable
internal data class RecordContentProvider(
    val backStackRecord: SaveableBackStack.Record,
    val content: @Composable (SaveableBackStack.Record) -> Unit,
)

@Composable
private fun SaveableBackStack.buildCircuitContentProviders(
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
