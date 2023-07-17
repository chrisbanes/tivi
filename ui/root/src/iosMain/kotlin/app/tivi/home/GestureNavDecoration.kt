// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.slack.circuit.runtime.Navigator
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.filter

@ExperimentalMaterialApi
@Immutable
class SwipeProperties(
    val enterScreenOffsetFraction: Float = 0.25f,
    val swipeThreshold: ThresholdConfig = FractionalThreshold(0.4f),
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
internal actual class GestureNavDecoration @ExperimentalMaterialApi constructor(
    private val navigator: Navigator,
    private val swipeProperties: SwipeProperties,
) : NavDecorationWithPrevious {

    @OptIn(ExperimentalMaterialApi::class)
    actual constructor(
        navigator: Navigator,
    ) : this(navigator, SwipeProperties())

    @Composable
    override fun <T> DecoratedContent(
        arg: T,
        previous: T?,
        backStackDepth: Int,
        modifier: Modifier,
        content: @Composable (T) -> Unit,
    ) {
        // Remember the previous stack depth so we know if the navigation is going "back".
        var prevStackDepth by rememberSaveable { mutableStateOf(backStackDepth) }
        SideEffect {
            prevStackDepth = backStackDepth
        }

        val dismissState = rememberDismissState()

        LaunchedEffect(dismissState) {
            snapshotFlow { dismissState.isDismissed(DismissDirection.StartToEnd) }
                .filter { it }
                .collect { navigator.pop() }
        }

        LaunchedEffect(arg) {
            // Each time the top record changes, reset the dismiss state.
            // We don't use reset() as that animates, and we need a snap.
            dismissState.snapTo(DismissValue.Default)
        }

        Box(modifier = modifier) {
            if (previous != null) {
                PreviousContent(
                    dismissState = dismissState,
                    swipeProperties = swipeProperties,
                    content = { content(previous) },
                )
            }

            AnimatedContent(
                targetState = arg,
                transitionSpec = {
                    when {
                        // adding to back stack
                        backStackDepth > prevStackDepth -> {
                            slideInHorizontally(initialOffsetX = End) with slideOutHorizontally(
                                targetOffsetX = { width ->
                                    0 - (swipeProperties.enterScreenOffsetFraction * width).roundToInt()
                                },
                            )
                        }

                        // come back from back stack
                        backStackDepth < prevStackDepth -> {
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
                    }
                },
                modifier = modifier,
                label = "",
            ) { record ->
                SwipeableContent(
                    state = dismissState,
                    swipeEnabled = backStackDepth > 1,
                    dismissThreshold = swipeProperties.swipeThreshold,
                    content = { content(record) },
                )
            }
        }
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
    Box(
        modifier = modifier
            .graphicsLayer {
                val offset = swipeProperties.enterScreenOffsetFraction *
                    (dismissState.offset.value.absoluteValue - size.width)
                IntOffset(x = offset.roundToInt(), y = 0)
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
