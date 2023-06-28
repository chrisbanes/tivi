@file:Suppress("NAME_SHADOWING")

package me.saket.swipe

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A composable that can be swiped left or right for revealing actions.
 *
 * @param swipeThreshold Minimum drag distance before any [SwipeAction] is
 * activated and can be swiped.
 *
 * @param backgroundUntilSwipeThreshold Color drawn behind the content until
 * [swipeThreshold] is reached. When the threshold is passed, this color is
 * replaced by the currently visible [SwipeAction]'s background.
 */
@Composable
fun SwipeableActionsBox(
  modifier: Modifier = Modifier,
  state: SwipeableActionsState = rememberSwipeableActionsState(),
  startActions: List<SwipeAction> = emptyList(),
  endActions: List<SwipeAction> = emptyList(),
  swipeThreshold: Dp = 40.dp,
  backgroundUntilSwipeThreshold: Color = Color.DarkGray,
  content: @Composable BoxScope.() -> Unit
) = BoxWithConstraints(modifier) {
  val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
  val leftActions = if (isRtl) endActions else startActions
  val rightActions = if (isRtl) startActions else endActions
  val swipeThresholdPx = LocalDensity.current.run { swipeThreshold.toPx() }

  val ripple = remember {
    SwipeRippleState()
  }
  val actions = remember(leftActions, rightActions) {
    ActionFinder(left = leftActions, right = rightActions)
  }
  LaunchedEffect(state, actions) {
    state.run {
      canSwipeTowardsRight = { leftActions.isNotEmpty() }
      canSwipeTowardsLeft = { rightActions.isNotEmpty() }
    }
  }

  val offset = state.offset.value
  val thresholdCrossed = abs(offset) > swipeThresholdPx

  var swipedAction: SwipeActionMeta? by remember {
    mutableStateOf(null)
  }
  val visibleAction: SwipeActionMeta? = remember(offset, actions) {
    actions.actionAt(offset, totalWidth = constraints.maxWidth)
  }
  val backgroundColor: Color by animateColorAsState(
    when {
      swipedAction != null -> swipedAction!!.value.background
      !thresholdCrossed -> backgroundUntilSwipeThreshold
      visibleAction == null -> Color.Transparent
      else -> visibleAction.value.background
    }
  )

  val scope = rememberCoroutineScope()
  Box(
    modifier = Modifier
      .absoluteOffset { IntOffset(x = offset.roundToInt(), y = 0) }
      .drawOverContent { ripple.draw(scope = this) }
      .draggable(
        orientation = Horizontal,
        enabled = !state.isResettingOnRelease,
        onDragStopped = {
          scope.launch {
            if (thresholdCrossed && visibleAction != null) {
              swipedAction = visibleAction
              swipedAction!!.value.onSwipe()
              ripple.animate(action = swipedAction!!)
            }
          }
          scope.launch {
            state.resetOffset()
            swipedAction = null
          }
        },
        state = state.draggableState,
      ),
    content = content
  )

  (swipedAction ?: visibleAction)?.let { action ->
    ActionIconBox(
      modifier = Modifier.matchParentSize(),
      action = action,
      offset = offset,
      backgroundColor = backgroundColor,
      content = { action.value.icon() }
    )
  }
}

@Composable
private fun ActionIconBox(
  action: SwipeActionMeta,
  offset: Float,
  backgroundColor: Color,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Row(
    modifier = modifier
      .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(width = placeable.width, height = placeable.height) {
          // Align icon with the left/right edge of the content being swiped.
          val iconOffset = if (action.isOnRightSide) constraints.maxWidth + offset else offset - placeable.width
          placeable.placeRelative(x = iconOffset.roundToInt(), y = 0)
        }
      }
      .background(color = backgroundColor),
    horizontalArrangement = if (action.isOnRightSide) Arrangement.Start else Arrangement.End,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    content()
  }
}

private fun Modifier.drawOverContent(onDraw: DrawScope.() -> Unit): Modifier {
  return drawWithContent {
    drawContent()
    onDraw(this)
  }
}
