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

import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.core.OnChildPositioned
import androidx.ui.core.OnPositioned
import androidx.ui.core.WithDensity
import androidx.ui.foundation.animation.animatedDragValue
import androidx.ui.foundation.gestures.DragDirection
import androidx.ui.foundation.gestures.Draggable
import androidx.ui.layout.Container
import androidx.ui.layout.Stack
import androidx.ui.unit.IntPx
import androidx.ui.unit.IntPxSize
import kotlin.math.absoluteValue

enum class SwipeDirection {
    LEFT, RIGHT
}

private val defaultDirections = listOf(SwipeDirection.LEFT, SwipeDirection.RIGHT)

@Composable
fun SwipeToDismiss(
    onSwipe: ((Float) -> Unit)? = null,
    onSwipeComplete: (SwipeDirection) -> Unit,
    swipeDirections: List<SwipeDirection> = defaultDirections,
    swipeCompletePercentage: Float = 0.6f,
    backgroundChildren: @Composable() (swipeProgress: Float, wouldCompleteOnRelease: Boolean) -> Unit,
    swipeChildren: @Composable() (swipeProgress: Float, wouldCompleteOnRelease: Boolean) -> Unit
) = Stack {
    val min = state { 0f }
    val max = state { 0f }

    OnPositioned(onPositioned = { coordinates ->
        if (SwipeDirection.LEFT in swipeDirections) {
            min.value = -coordinates.size.width.value.toFloat()
        }
        if (SwipeDirection.RIGHT in swipeDirections) {
            max.value = coordinates.size.width.value.toFloat()
        }
    })

    val position = animatedDragValue(0f, min.value, max.value)
    val swipeChildrenSize = state { IntPxSize(IntPx.Zero, IntPx.Zero) }
    val progress = state { 0f }

    if (position.value != 0f) {
        // Container only accepts Dp values for it's size, whereas we have Px values. So we
        // need to use WithDensity to convert them back.
        // TODO: raise FR so that Container can accept Px/PxSize values
        WithDensity {
            Container(
                width = swipeChildrenSize.value.width.toDp(),
                height = swipeChildrenSize.value.height.toDp(),
                children = {
                    backgroundChildren(
                        progress.value,
                        progress.value.absoluteValue >= swipeCompletePercentage
                    )
                }
            )
        }
    }

    OnChildPositioned(onPositioned = { coords ->
        swipeChildrenSize.value = coords.size
    }) {
        Draggable(
            dragDirection = DragDirection.Horizontal,
            dragValue = position,
            onDragStopped = {
                // We can't reference AnimatedValueHolder.animatedFloat.min/max so we have to keep
                // our own state /sadface
                // TODO: raise FR to open up AnimatedFloat.min/max
                if (max.value > 0f && position.value / max.value >= swipeCompletePercentage) {
                    onSwipeComplete(SwipeDirection.RIGHT)
                } else if (min.value < 0f && position.value / min.value >= swipeCompletePercentage) {
                    onSwipeComplete(SwipeDirection.LEFT)
                }
                position.animatedFloat.animateTo(0f)
            },
            onDragValueChangeRequested = { dragValue ->
                // Update the position using snapTo
                position.animatedFloat.snapTo(dragValue)

                progress.value = when {
                    dragValue < 0f && min.value < 0f -> dragValue / min.value
                    dragValue > 0f && max.value > 0f -> dragValue / max.value
                    else -> 0f
                }

                if (onSwipe != null) {
                    // If we have an onSwipe callback, invoke it
                    onSwipe(progress.value)
                }
            }
        ) {
            WithOffset(xOffset = position) {
                swipeChildren(
                    progress.value,
                    progress.value.absoluteValue >= swipeCompletePercentage
                )
            }
        }
    }
}
