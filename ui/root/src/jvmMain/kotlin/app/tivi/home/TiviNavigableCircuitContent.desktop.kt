// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.ProvidedValues
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen

@Composable
actual fun TiviNavigableCircuitContent(
    navigator: Navigator,
    backstack: SaveableBackStack,
    modifier: Modifier,
    circuitConfig: CircuitConfig,
    providedValues: Map<out BackStack.Record, ProvidedValues>,
    unavailableRoute: @Composable (screen: Screen, modifier: Modifier) -> Unit,
) {
    // Just use the default on Desktop
    NavigableCircuitContent(
        navigator = navigator,
        backstack = backstack,
        modifier = modifier,
        circuitConfig = circuitConfig,
        providedValues = providedValues,
    )
}
