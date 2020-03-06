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
import androidx.compose.onCommit
import androidx.compose.state
import androidx.ui.animation.animatedFloat
import androidx.ui.core.DensityAmbient
import androidx.ui.core.LayoutDirection
import androidx.ui.core.LayoutDirectionAmbient
import androidx.ui.core.OnPositioned
import androidx.ui.foundation.gestures.DragDirection
import androidx.ui.foundation.gestures.Draggable
import androidx.ui.layout.Container
import androidx.ui.layout.Stack
import androidx.ui.unit.IntPx
import androidx.ui.unit.IntPxSize
import kotlin.math.absoluteValue

enum class SwipeDirection {
    START, END
}

private val defaultDirections = listOf(SwipeDirection.START, SwipeDirection.END)

@Composable
fun SwipeToDismiss(
    onSwipe: ((Float) -> Unit)? = null,
    onSwipeComplete: (SwipeDirection) -> Unit,
    swipeDirections: List<SwipeDirection> = defaultDirections,
    swipeCompletePercentage: Float = 0.6f,
    backgroundChildren: @Composable() (swipeProgress: Float, wouldCompleteOnRelease: Boolean) -> Unit,
    swipeChildren: @Composable() (swipeProgress: Float, wouldCompleteOnRelease: Boolean) -> Unit
) = Stack {
    val position = animatedFloat(initVal = 0f).apply {
        setBounds(0f, 0f)
    }
    var size by state { IntPxSize(IntPx.Zero, IntPx.Zero) }
    var progress by state { 0f }

    val layoutDir = LayoutDirectionAmbient.current

    OnPositioned { coordinates ->
        size = coordinates.size

        when {
            SwipeDirection.START in swipeDirections && SwipeDirection.END in swipeDirections -> {
                position.setBounds(
                    -coordinates.size.width.value.toFloat(),
                    coordinates.size.width.value.toFloat()
                )
            }
            SwipeDirection.START in swipeDirections && layoutDir == LayoutDirection.Ltr ||
                SwipeDirection.END in swipeDirections && layoutDir == LayoutDirection.Rtl -> {
                position.setBounds(-coordinates.size.width.value.toFloat(), 0f)
            }
            SwipeDirection.END in swipeDirections && layoutDir == LayoutDirection.Ltr ||
                SwipeDirection.START in swipeDirections && layoutDir == LayoutDirection.Rtl -> {
                position.setBounds(0f, coordinates.size.width.value.toFloat())
            }
        }
    }

    if (position.value != 0f) {
        with(DensityAmbient.current) {
            Container(width = size.width.toDp(), height = size.height.toDp()) {
                backgroundChildren(progress, progress.absoluteValue >= swipeCompletePercentage)
            }
        }
    }

    Draggable(
        dragDirection = DragDirection.Horizontal,
        dragValue = position,
        onDragStopped = {
            when {
                position.max > 0f && position.value / position.max >= swipeCompletePercentage -> {
                    position.animateTo(position.max, onEnd = { endReason, _ ->
                        if (endReason != AnimationEndReason.Interrupted) {
                            onSwipeComplete(
                                when (layoutDir) {
                                    LayoutDirection.Ltr -> SwipeDirection.END
                                    LayoutDirection.Rtl -> SwipeDirection.START
                                }
                            )
                        }
                    })
                }
                position.min < 0f && position.value / position.min >= swipeCompletePercentage -> {
                    position.animateTo(position.min, onEnd = { endReason, _ ->
                        if (endReason != AnimationEndReason.Interrupted) {
                            onSwipeComplete(
                                when (layoutDir) {
                                    LayoutDirection.Ltr -> SwipeDirection.START
                                    LayoutDirection.Rtl -> SwipeDirection.END
                                }
                            )
                        }
                    })
                }
                else -> position.animateTo(0f)
            }
        },
        onDragValueChangeRequested = { position.snapTo(it) }
    ) {
        onCommit(position.value) {
            // When the position changes, update the progress and call onSwipe
            progress = when {
                position.value < 0f && position.min < 0f -> position.value / position.min
                position.value > 0f && position.max > 0f -> position.value / position.max
                else -> 0f
            }
            // If we have an onSwipe callback, invoke it
            onSwipe?.invoke(progress)
        }

        WithOffset(xOffset = position) {
            swipeChildren(progress, progress.absoluteValue >= swipeCompletePercentage)
        }
    }
}
