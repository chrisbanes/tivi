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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.LayoutModifier
import androidx.compose.ui.Measurable
import androidx.compose.ui.MeasureScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.OnPositionedModifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import kotlin.math.roundToInt

inline val LayoutCoordinates.positionInParent: Offset
    get() = parentCoordinates?.childToLocal(this, Offset.Zero) ?: Offset.Zero

inline val LayoutCoordinates.boundsInParent: Rect
    get() = Rect(positionInParent, size.toSize())

fun Modifier.onSizeChanged(
    onChange: (IntSize) -> Unit
) = composed {
    var lastSize by rememberMutableState<IntSize?> { null }

    object : OnPositionedModifier {
        override fun onPositioned(coordinates: LayoutCoordinates) {
            if (coordinates.size != lastSize) {
                lastSize = coordinates.size
                onChange(coordinates.size)
            }
        }
    }
}

fun Modifier.onPositionInParentChanged(
    onChange: (LayoutCoordinates) -> Unit
) = composed {
    var lastPosition by rememberMutableState<Offset?> { null }

    object : OnPositionedModifier {
        override fun onPositioned(coordinates: LayoutCoordinates) {
            if (coordinates.positionInParent != lastPosition) {
                lastPosition = coordinates.positionInParent
                onChange(coordinates)
            }
        }
    }
}

fun Modifier.onPositionInRootChanged(
    onChange: (LayoutCoordinates) -> Unit
) = composed {
    var lastPosition by rememberMutableState<Offset?> { null }

    object : OnPositionedModifier {
        override fun onPositioned(coordinates: LayoutCoordinates) {
            if (coordinates.positionInRoot != lastPosition) {
                lastPosition = coordinates.positionInRoot
                onChange(coordinates)
            }
        }
    }
}

fun Modifier.offset(getOffset: (IntSize) -> Offset) = then(OffsetModifier(getOffset))

private data class OffsetModifier(
    private val getOffset: (IntSize) -> Offset
) : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureScope.MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            val offset = getOffset(IntSize(placeable.width, placeable.height))
            placeable.place(offset.x.roundToInt(), offset.y.roundToInt())
        }
    }
}
