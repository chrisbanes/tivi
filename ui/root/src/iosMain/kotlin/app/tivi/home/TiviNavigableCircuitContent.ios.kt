// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.ProvidedValues
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.isEmpty
import com.slack.circuit.backstack.providedValuesForBackStack
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.LocalCircuitConfig
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen

@Composable
actual fun TiviNavigableCircuitContent(
    navigator: Navigator,
    backstack: SaveableBackStack,
    modifier: Modifier,
    circuitConfig: CircuitConfig,
    providedValues: Map<out BackStack.Record, ProvidedValues>,
    // decoration: NavDecoration = circuitConfig.defaultNavDecoration,
    unavailableRoute: @Composable (screen: Screen, modifier: Modifier) -> Unit,
) {
    @OptIn(ExperimentalMaterialApi::class)
    TiviNavigableCircuitContent(
        navigator = navigator,
        backstack = backstack,
        swipeProperties = SwipeProperties(),
        modifier = modifier,
        circuitConfig = circuitConfig,
        providedValues = providedValues,
        unavailableRoute = unavailableRoute,
    )
}

@ExperimentalMaterialApi
@Composable
fun TiviNavigableCircuitContent(
    navigator: Navigator,
    backstack: SaveableBackStack,
    swipeProperties: SwipeProperties,
    modifier: Modifier = Modifier,
    circuitConfig: CircuitConfig = requireNotNull(LocalCircuitConfig.current),
    providedValues: Map<out BackStack.Record, ProvidedValues> = providedValuesForBackStack(backstack),
    // decoration: NavDecoration = circuitConfig.defaultNavDecoration,
    unavailableRoute: (@Composable (screen: Screen, modifier: Modifier) -> Unit) =
        circuitConfig.onUnavailableContent,
) {
    if (backstack.isEmpty) return

    val activeContentProviders = backstack.buildCircuitContentProviders(
        navigator = navigator,
        circuitConfig = circuitConfig,
        unavailableRoute = unavailableRoute,
    )

    SwipeDismissContent(
        current = activeContentProviders.first(),
        previous = activeContentProviders.getOrNull(1),
        onCurrentDismissed = { navigator.pop() },
        swipeProperties = swipeProperties,
        modifier = modifier,
    ) { contentProvider ->
        val values = providedValues[contentProvider.backStackRecord]?.provideValues()
        val providedLocals = remember(values) {
            values?.toTypedArray() ?: emptyArray()
        }
        CompositionLocalProvider(*providedLocals) {
            contentProvider.content(contentProvider.backStackRecord)
        }
    }
}
