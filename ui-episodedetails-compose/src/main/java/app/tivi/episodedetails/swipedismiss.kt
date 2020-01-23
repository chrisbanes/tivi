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

package app.tivi.episodedetails

import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.core.OnPositioned
import androidx.ui.foundation.animation.animatedDragValue
import androidx.ui.foundation.gestures.DragDirection
import androidx.ui.foundation.gestures.Draggable

enum class SwipeDirection {
    LEFT, RIGHT
}

@Composable
fun SwipeToDismiss(
    onSwipe: ((Float) -> Unit)? = null,
    onSwipeComplete: (SwipeDirection) -> Unit,
    swipeCompletePercentage: Float = 0.5f,
    backgroundChildren: @Composable() () -> Unit,
    swipeChildren: @Composable() () -> Unit
) {
    // We can't reference AnimatedValueHolder.animatedFloat.min/max so we have to keep
    // our own state /sadface
    val width = state { 0f }

    OnPositioned(onPositioned = { coordinates ->
        width.value = coordinates.size.width.value
    })

    val position = animatedDragValue(0f, -width.value, width.value)

    Draggable(
        dragDirection = DragDirection.Horizontal,
        dragValue = position,
        onDragStopped = {
            // Ideally we'd reference position.animatedFloat.min/max directly here
            if (position.value / width.value >= swipeCompletePercentage) {
                onSwipeComplete(SwipeDirection.RIGHT)
            } else if (position.value / -width.value >= swipeCompletePercentage) {
                onSwipeComplete(SwipeDirection.LEFT)
            }
            position.animatedFloat.animateTo(0f)
        },
        onDragValueChangeRequested = {
            position.animatedFloat.snapTo(it)

            if (onSwipe != null) {
                val v = position.value
                when {
                    v < 0f -> onSwipe(v / -width.value)
                    else -> onSwipe(v / width.value)
                }
            }
        }
    ) {
        if (position.value != 0f) {
            backgroundChildren()
        }
        WithOffset(xOffset = position) {
            swipeChildren()
        }
    }
}
