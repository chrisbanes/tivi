/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.common.compose

import androidx.animation.AnimationEndReason
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.animation.animatedFloat
import androidx.ui.core.DensityAmbient
import androidx.ui.core.LayoutDirection
import androidx.ui.core.Modifier
import androidx.ui.core.WithConstraints
import androidx.ui.foundation.Box
import androidx.ui.foundation.gestures.DragDirection
import androidx.ui.foundation.gestures.draggable
import androidx.ui.layout.Stack
import androidx.ui.layout.offset
import androidx.ui.unit.dp
import app.tivi.common.compose.SwipeDirection.END
import app.tivi.common.compose.SwipeDirection.START
import kotlin.math.absoluteValue

enum class SwipeDirection {
    START, END
}

private val defaultDirections = listOf(START, END)

@Composable
fun SwipeToDismiss(
    onSwipe: ((Float) -> Unit)? = null,
    onSwipeComplete: (SwipeDirection) -> Unit,
    swipeDirections: List<SwipeDirection> = defaultDirections,
    swipeCompletePercentage: Float = 0.6f,
    backgroundChildren: @Composable (swipeProgress: Float, wouldCompleteOnRelease: Boolean) -> Unit,
    swipeChildren: @Composable (swipeProgress: Float, wouldCompleteOnRelease: Boolean) -> Unit
) = Stack {
    val position = animatedFloat(initVal = 0f).apply { setBounds(0f, 0f) }
    var progress by state { 0f }

    Box(modifier = Modifier.matchParentSize()) {
        backgroundChildren(progress, progress.absoluteValue >= swipeCompletePercentage)
    }

    WithConstraints {
        // Update the drag bounds depending on the size
        when {
            START in swipeDirections && END in swipeDirections -> {
                position.setBounds(-constraints.maxWidth.toFloat(), constraints.maxWidth.toFloat())
            }
            layoutDirection == LayoutDirection.Ltr && START in swipeDirections
                || layoutDirection == LayoutDirection.Rtl && END in swipeDirections -> {
                position.setBounds(-constraints.maxWidth.toFloat(), 0f)
            }
            layoutDirection == LayoutDirection.Ltr && END in swipeDirections
                || layoutDirection == LayoutDirection.Rtl && START in swipeDirections -> {
                position.setBounds(0f, constraints.maxWidth.toFloat())
            }
        }

        val draggable = Modifier.draggable(
            dragDirection = DragDirection.Horizontal,
            onDragStopped = {
                // TODO: look at using fling and velocity here
                if (position.max > 0f && position.value / position.max >= swipeCompletePercentage) {
                    position.animateTo(
                        position.max,
                        onEnd = { endReason, _ ->
                            if (endReason != AnimationEndReason.Interrupted) {
                                onSwipeComplete(if (layoutDirection == LayoutDirection.Ltr) END else START)
                            }
                        }
                    )
                } else if (position.min < 0f && position.value / position.min >= swipeCompletePercentage) {
                    position.animateTo(
                        position.min,
                        onEnd = { endReason, _ ->
                            if (endReason != AnimationEndReason.Interrupted) {
                                onSwipeComplete(if (layoutDirection == LayoutDirection.Ltr) START else END)
                            }
                        }
                    )
                } else position.animateTo(0f)
            }
        ) { delta ->
            val oldPosition = position.value
            position.snapTo(oldPosition + delta)
            val newPosition = position.value

            progress = when {
                newPosition < 0f && position.min < 0f -> newPosition / position.min
                newPosition > 0f && position.max > 0f -> newPosition / position.max
                else -> 0f
            }
            // If we have an onSwipe callback, invoke it
            onSwipe?.invoke(progress)

            // Return the difference in position (delta)
            newPosition - oldPosition
        }

        val xOffset = with(DensityAmbient.current) { position.value.toDp() }
        Box(
            modifier = Modifier.plus(draggable).offset(x = xOffset, y = 0.dp),
            children = {
                swipeChildren(progress, progress.absoluteValue >= swipeCompletePercentage)
            }
        )
    }
}
