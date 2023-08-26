// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import app.tivi.circuit.TiviBackStack
import app.tivi.circuit.screen
import app.tivi.extensions.fluentIf
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.ProvidedValues
import com.slack.circuit.backstack.providedValuesForBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.LocalCircuit
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
    backstack: TiviBackStack,
    modifier: Modifier = Modifier,
    circuit: Circuit = requireNotNull(LocalCircuit.current),
    providedValues: Map<out BackStack.Record, ProvidedValues> = providedValuesForBackStack(backstack),
    decoration: NavDecorationWithPrevious,
    unavailableRoute: (@Composable (screen: Screen, modifier: Modifier) -> Unit) =
        circuit.onUnavailableContent,
) {
    val activeContentProviders = backstack.buildCircuitContentProviders(
        navigator = navigator,
        circuit = circuit,
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
    val backStackRecord: TiviBackStack.Record,
    val content: @Composable (TiviBackStack.Record) -> Unit,
)

@Composable
private fun TiviBackStack.buildCircuitContentProviders(
    navigator: Navigator,
    circuit: Circuit,
    unavailableRoute: @Composable (screen: Screen, modifier: Modifier) -> Unit,
): List<RecordContentProvider> {
    val previousContentProviders = remember { mutableMapOf<String, RecordContentProvider>() }

    val lastNavigator by rememberUpdatedState(navigator)
    val lastCircuit by rememberUpdatedState(circuit)
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
                            circuit = lastCircuit,
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

@Composable
internal fun PreviousContent(
    isVisible: () -> Boolean = { true },
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            // If we're not visible, don't measure, layout (or draw)
            .fluentIf(!isVisible()) { emptyLayout() }
            // Content in the back stack should not be interactive until they're on top
            .pointerInput(Unit) {},
    ) {
        content()
    }
}

/**
 * This no-ops measure + layout (and thus draw) for child content.
 */
private fun Modifier.emptyLayout(): Modifier = layout { _, constraints ->
    layout(constraints.minWidth, constraints.minHeight) {}
}
