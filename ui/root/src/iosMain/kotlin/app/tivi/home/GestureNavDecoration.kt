// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.Navigator
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.filter

@ExperimentalMaterialApi
@Immutable
class SwipeProperties(
    val enterScreenOffsetFraction: Float = 0.25f,
    val swipeThreshold: ThresholdConfig = FractionalThreshold(0.4f),
    val swipeAreaWidth: Dp = 16.dp,
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
        Box(modifier = modifier) {
            // Remember the previous stack depth so we know if the navigation is going "back".
            var prevStackDepth by rememberSaveable { mutableStateOf(backStackDepth) }
            SideEffect {
                prevStackDepth = backStackDepth
            }

            val dismissState = rememberDismissState(arg)
            var offsetWhenPopped by remember { mutableStateOf(0f) }

            LaunchedEffect(dismissState) {
                snapshotFlow { dismissState.isDismissed(DismissDirection.StartToEnd) }
                    .filter { it }
                    .collect {
                        navigator.pop()
                        offsetWhenPopped = dismissState.offset.value
                    }
            }

            val transition = updateTransition(targetState = arg, label = "GestureNavDecoration")

            if (previous != null) {
                // Previous content is only visible if the swipe-dismiss offset != 0
                val showPrevious by remember(dismissState) {
                    derivedStateOf { dismissState.offset.value != 0f || transition.isRunning }
                }

                PreviousContent(
                    isVisible = { showPrevious },
                    modifier = Modifier.graphicsLayer {
                        translationX = (dismissState.offset.value.absoluteValue - size.width) *
                            swipeProperties.enterScreenOffsetFraction
                    },
                    content = { content(previous) },
                )
            }

            transition.AnimatedContent(
                transitionSpec = {
                    when {
                        // adding to back stack
                        backStackDepth > prevStackDepth -> {
                            slideInHorizontally(initialOffsetX = End) togetherWith slideOutHorizontally(
                                targetOffsetX = { width ->
                                    0 - (swipeProperties.enterScreenOffsetFraction * width).roundToInt()
                                },
                            )
                        }

                        // come back from back stack
                        backStackDepth < prevStackDepth -> {
                            when {
                                offsetWhenPopped != 0f -> {
                                    // If the record change was caused by a swipe gesture, let's
                                    // jump cut
                                    EnterTransition.None togetherWith ExitTransition.None
                                }

                                else -> {
                                    slideInHorizontally { width ->
                                        0 - (swipeProperties.enterScreenOffsetFraction * width).roundToInt()
                                    }
                                        .togetherWith(slideOutHorizontally(targetOffsetX = End))
                                        .apply { targetContentZIndex = -1f }
                                }
                            }
                        }

                        // Root reset. Crossfade
                        else -> fadeIn() togetherWith fadeOut()
                    }
                },
                modifier = modifier,
            ) { record ->
                SwipeableContent(
                    state = dismissState,
                    swipeEnabled = backStackDepth > 1,
                    swipeAreaWidth = swipeProperties.swipeAreaWidth,
                    dismissThreshold = swipeProperties.swipeThreshold,
                    content = { content(record) },
                )
            }

            LaunchedEffect(arg) {
                // Reset the offsetWhenPopped when the top record changes
                offsetWhenPopped = 0f
            }
        }
    }
}

private val End: (Int) -> Int = { it }

/**
 * This is basically [androidx.compose.material.SwipeToDismiss] but simplified for our
 * use case.
 */
@Composable
@ExperimentalMaterialApi
internal fun SwipeableContent(
    state: DismissState,
    swipeAreaWidth: Dp,
    dismissThreshold: ThresholdConfig,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier) {
        val width = constraints.maxWidth

        val nestedScrollConnection = remember(state) {
            SwipeDismissNestedScrollConnection(state)
        }

        // All credit to @alexzhirkevich for this hack to reduce the Modifier.swipeable() hit area
        // https://github.com/alexzhirkevich/compose-look-and-feel/blob/master/lookandfeel/src/commonMain/kotlin/moe/tlaster/precompose/navigation/NavHost.kt
        val shift = with(LocalDensity.current) {
            remember(this, width, swipeAreaWidth) {
                width - swipeAreaWidth.roundToPx().coerceIn(0, width)
            }
        }

        Box(
            modifier = Modifier
                .let { modifier ->
                    when {
                        swipeEnabled -> modifier.nestedScroll(nestedScrollConnection)
                        else -> modifier
                    }
                }
                // Offset so only the end-most swipeAreaWidth is visible
                .offset { IntOffset(x = -shift, y = 0) }
                .swipeable(
                    state = state,
                    anchors = mapOf(
                        0f to DismissValue.Default,
                        width.toFloat() to DismissValue.DismissedToEnd,
                    ),
                    thresholds = { _, _ -> dismissThreshold },
                    orientation = Orientation.Horizontal,
                    enabled = swipeEnabled,
                    reverseDirection = LocalLayoutDirection.current == LayoutDirection.Rtl,
                    resistance = ResistanceConfig(
                        basis = width.toFloat(),
                        factorAtMin = SwipeableDefaults.StiffResistanceFactor,
                        factorAtMax = SwipeableDefaults.StandardResistanceFactor,
                    ),
                )
                // Offset back to origin
                .offset { IntOffset(x = shift, y = 0) },
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

@OptIn(ExperimentalMaterialApi::class)
private class SwipeDismissNestedScrollConnection(
    private val state: DismissState,
) : NestedScrollConnection {
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset = when {
        available.x < 0 && source == NestedScrollSource.Drag -> {
            // If we're being swiped back to origin, let the SwipeDismiss handle it first
            Offset(x = state.performDrag(available.x), y = 0f)
        }

        else -> Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset = when (source) {
        NestedScrollSource.Drag -> Offset(x = state.performDrag(available.x), y = 0f)
        else -> Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity = when {
        available.x > 0 && state.offset.value > 0 -> {
            state.performFling(velocity = available.x)
            available
        }

        else -> Velocity.Zero
    }

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity,
    ): Velocity {
        state.performFling(velocity = available.x)
        return available
    }
}

@Composable
@ExperimentalMaterialApi
private fun rememberDismissState(
    vararg inputs: Any?,
    initialValue: DismissValue = DismissValue.Default,
    confirmStateChange: (DismissValue) -> Boolean = { true },
): DismissState {
    return rememberSaveable(inputs, saver = DismissState.Saver(confirmStateChange)) {
        DismissState(initialValue, confirmStateChange)
    }
}
