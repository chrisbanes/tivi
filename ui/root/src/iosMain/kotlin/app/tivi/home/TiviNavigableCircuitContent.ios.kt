// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
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
import androidx.compose.material.rememberDismissState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.ProvidedValues
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.isAtRoot
import com.slack.circuit.backstack.isEmpty
import com.slack.circuit.backstack.providedValuesForBackStack
import com.slack.circuit.foundation.CircuitConfig
import com.slack.circuit.foundation.LocalCircuitConfig
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.filter

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
@Immutable
class SwipeProperties(
    val enterScreenOffsetFraction: Float = 0.25f,
    val swipeThreshold: ThresholdConfig = FractionalThreshold(0.4f),
)

@OptIn(ExperimentalAnimationApi::class)
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

    val dismissState = rememberDismissState()

    LaunchedEffect(dismissState) {
        snapshotFlow { dismissState.isDismissed(DismissDirection.StartToEnd) }
            .filter { it }
            .collect {
                navigator.pop()
            }
    }

    LaunchedEffect(backstack.topRecord) {
        // Each time the top record changes, reset the dismiss state.
        // We don't use reset() as that animates, and we need a snap.
        dismissState.snapTo(DismissValue.Default)
    }

    GestureDrivenNavigableCircuitContent(
        navigator = navigator,
        backstack = backstack,
        modifier = modifier,
        circuitConfig = circuitConfig,
        providedValues = providedValues,
        transitionSpec = { diff ->
            when {
                // adding to back stack
                diff > 0 -> {
                    slideInHorizontally(initialOffsetX = End) with slideOutHorizontally(
                        targetOffsetX = { width ->
                            0 - (swipeProperties.enterScreenOffsetFraction * width).roundToInt()
                        },
                    )
                }

                // come back from back stack
                diff < 0 -> {
                    if (dismissState.offset.value != 0f) {
                        EnterTransition.None with ExitTransition.None
                    } else {
                        slideInHorizontally { width ->
                            0 - (swipeProperties.enterScreenOffsetFraction * width).roundToInt()
                        }.with(slideOutHorizontally(targetOffsetX = End))
                            .apply {
                                targetContentZIndex = -1f
                            }
                    }
                }

                // Root reset. Crossfade
                else -> fadeIn() with fadeOut()
            }.apply {
                // Disable clipping since the faded slide-in/out should
                // be displayed out of bounds.
                SizeTransform(clip = false)

                targetContentZIndex = if (diff < 0) -1f else 0f
            }
        },
        unavailableRoute = unavailableRoute,
        previousContent = {
            PreviousContent(
                dismissState = dismissState,
                swipeProperties = swipeProperties,
                content = { content(backStackRecord) },
            )
        },
    ) {
        LaunchedEffect(dismissState) {
            snapshotFlow { dismissState.isDismissed(DismissDirection.StartToEnd) }
                .filter { it }
                .collect { poppedViaGesture = true }
        }

        SwipeableContent(
            state = dismissState,
            swipeEnabled = !backstack.isAtRoot,
            dismissThreshold = swipeProperties.swipeThreshold,
            content = { content(backStackRecord) },
        )
    }
}

private val End: (Int) -> Int = { it }

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun PreviousContent(
    dismissState: DismissState,
    swipeProperties: SwipeProperties,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var width by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .onSizeChanged { width = it.width }
            .offset {
                val offset = dismissState.offset.value
                val offsetX = swipeProperties.enterScreenOffsetFraction * (offset.absoluteValue - width)
                IntOffset(x = offsetX.roundToInt(), y = 0)
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
internal fun SwipeableContent(
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
                enabled = swipeEnabled,
                reverseDirection = LocalLayoutDirection.current == LayoutDirection.Rtl,
                resistance = ResistanceConfig(
                    basis = width,
                    factorAtMin = SwipeableDefaults.StiffResistanceFactor,
                    factorAtMax = SwipeableDefaults.StandardResistanceFactor,
                ),
            ),
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = state.offset.value.roundToInt(), y = 0) },
            ) {
                content()
            }
        }
    }
}
