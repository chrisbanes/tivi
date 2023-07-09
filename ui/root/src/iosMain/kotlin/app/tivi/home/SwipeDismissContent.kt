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
import androidx.compose.material.rememberDismissState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.filter

@ExperimentalMaterialApi
@Immutable
class SwipeProperties(
    val enterScreenOffsetFraction: Float = 0.25f,
    val swipeThreshold: ThresholdConfig = FractionalThreshold(0.4f),
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun <T> SwipeDismissContent(
    current: T,
    previous: T?,
    onCurrentDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    swipeProperties: SwipeProperties = SwipeProperties(),
    content: @Composable (T) -> Unit,
) {
    Box(modifier = modifier) {
        val dismissState = rememberDismissState()
        val lastOnCurrentDismissed by rememberUpdatedState(onCurrentDismissed)

        LaunchedEffect(dismissState) {
            snapshotFlow { dismissState.isDismissed(DismissDirection.StartToEnd) }
                .filter { it }
                .collect { lastOnCurrentDismissed() }
        }

        LaunchedEffect(current) {
            // Each time the current item changes, reset the dismiss state.
            // We don't use reset() as that animates, and we need a snap.
            dismissState.snapTo(DismissValue.Default)
        }

        // val showPrevious by remember(dismissState) {
        //     derivedStateOf { dismissState.offset.value != 0f }
        // }

        previous?.let { p ->
            // movableContentOf needs to be attached to the composition otherwise it is cleaned up.
            // Ideally we wouldn't call PreviousContent when `showPrevious` is false, but then we
            // lose all of the presenter state (as the movable content is cleaned-up).
            PreviousContent(
                dismissState = dismissState,
                swipeProperties = swipeProperties,
                modifier = Modifier.zIndex(0f),
                content = { content(p) },
            )
        }

        SwipeableContent(
            state = dismissState,
            swipeEnabled = previous != null,
            dismissThreshold = swipeProperties.swipeThreshold,
            modifier = Modifier.zIndex(1f),
            content = { content(current) },
        )
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
