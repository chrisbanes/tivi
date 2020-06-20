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

import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.LayoutCoordinates
import androidx.ui.core.Modifier
import androidx.ui.core.OnPositionedModifier
import androidx.ui.core.composed
import androidx.ui.core.positionInRoot
import androidx.ui.geometry.Offset
import androidx.ui.unit.IntSize
import androidx.ui.unit.PxBounds
import androidx.ui.unit.toSize

inline val LayoutCoordinates.positionInParent: Offset
    get() = parentCoordinates?.childToLocal(this, Offset.Zero) ?: Offset.Zero

inline val LayoutCoordinates.boundsInParent: PxBounds
    get() = PxBounds(positionInParent, size.toSize())

fun Modifier.onSizeChanged(
    onChange: (IntSize) -> Unit
) = composed {
    var lastSize by state<IntSize?> { null }

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
    var lastPosition by state<Offset?> { null }

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
    var lastPosition by state<Offset?> { null }

    object : OnPositionedModifier {
        override fun onPositioned(coordinates: LayoutCoordinates) {
            if (coordinates.positionInRoot != lastPosition) {
                lastPosition = coordinates.positionInRoot
                onChange(coordinates)
            }
        }
    }
}
