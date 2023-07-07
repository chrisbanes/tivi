// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.ResistanceConfig
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.ThresholdConfig
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.ProvidedValues
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.isEmpty
import com.slack.circuit.backstack.providedValuesForBackStack
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.LocalCircuitConfig
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterialApi::class)
@Immutable
class SwipeProperties(
    val enterScreenOffsetFraction: Float = 0.25f,
    val swipeThreshold: ThresholdConfig = FractionalThreshold(0.4f),
)

@OptIn(ExperimentalMaterialApi::class)
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

@OptIn(ExperimentalMaterialApi::class)
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

    val activeContentProviders = buildCircuitContentProvider(
        navigator = navigator,
        backstack = backstack,
        circuitConfig = circuitConfig,
        unavailableRoute = unavailableRoute,
    )

    val topContentProvider = activeContentProviders.first()
    val previousContentProvider = activeContentProviders.getOrNull(1)

    Box(modifier = modifier) {
        val dismissState = rememberDismissState(topContentProvider.backStackRecord.key)

        LaunchedEffect(dismissState) {
            snapshotFlow { dismissState.isDismissed(DismissDirection.StartToEnd) }
                .filter { it }
                .collect {
                    previousContentProvider?.wasSwiped = true
                    navigator.pop()
                }
        }

        val showPrevious by remember(dismissState) {
            derivedStateOf { dismissState.offset.value != 0f }
        }

        if (showPrevious) {
            previousContentProvider?.let { cp ->
                PreviousContent(
                    dismissState = dismissState,
                    swipeProperties = swipeProperties,
                    modifier = Modifier.zIndex(0f),
                ) {
                    val values = providedValues[cp.backStackRecord]?.provideValues()
                    val providedLocals = remember(values) {
                        values?.toTypedArray() ?: emptyArray()
                    }
                    CompositionLocalProvider(*providedLocals) { cp.content() }
                }
            }
        }

        SwipeableContent(
            state = dismissState,
            swipeEnabled = previousContentProvider != null,
            dismissThreshold = swipeProperties.swipeThreshold,
            modifier = Modifier.zIndex(if (showPrevious) 1f else 0f),
        ) {
            val values = providedValues[topContentProvider.backStackRecord]?.provideValues()
            val providedLocals = remember(values) {
                values?.toTypedArray() ?: emptyArray()
            }
            CompositionLocalProvider(*providedLocals) { topContentProvider.content() }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private inline fun PreviousContent(
    dismissState: DismissState,
    swipeProperties: SwipeProperties,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                val width = size.width.toInt()
                val offset = dismissState.offset.value
                translationX = -swipeProperties.enterScreenOffsetFraction * (width - offset.absoluteValue)
            }
            .pointerInput(Unit) {
                // Content in the back stack should not be interactive until they're on top
            },
    ) {
        content()
    }
}

/**
 * This is basically [androidx.compose.material.SwipeToDismiss] but simplified for our
 * use case.
 */
@Composable
@ExperimentalMaterialApi
private fun SwipeableContent(
    state: DismissState,
    dismissThreshold: ThresholdConfig,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier) {
        val width = constraints.maxWidth.toFloat()

        Box(
            modifier = Modifier.swipeable(
                state = state,
                anchors = mapOf(
                    0f to DismissValue.Default,
                    width to DismissValue.DismissedToEnd,
                ),
                thresholds = { _, _ -> dismissThreshold },
                orientation = Orientation.Horizontal,
                enabled = swipeEnabled, // && state.currentValue == DismissValue.Default,
                reverseDirection = LocalLayoutDirection.current == LayoutDirection.Rtl,
                resistance = ResistanceConfig(
                    basis = width,
                    factorAtMin = SwipeableDefaults.StiffResistanceFactor,
                    factorAtMax = SwipeableDefaults.StandardResistanceFactor,
                ),
            ),
        ) {
            Box(
                modifier = Modifier.offset {
                    IntOffset(x = state.offset.value.roundToInt(), y = 0)
                },
            ) {
                content()
            }
        }
    }
}

/**
 * Version of [rememberDismissState] which accept inputs (keys).
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun rememberDismissState(
    vararg inputs: Any?,
    initialValue: DismissValue = DismissValue.Default,
    confirmStateChange: (DismissValue) -> Boolean = { true },
): DismissState = rememberSaveable(
    inputs = inputs,
    saver = DismissState.Saver(confirmStateChange),
    init = { DismissState(initialValue, confirmStateChange) },
)
