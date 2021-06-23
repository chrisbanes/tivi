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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize

inline val LayoutCoordinates.positionInParent: Offset
    get() = parentCoordinates?.localPositionOf(this, Offset.Zero) ?: Offset.Zero

inline val LayoutCoordinates.boundsInParent: Rect
    get() = Rect(positionInParent, size.toSize())

fun Modifier.onPositionInParentChanged(
    onChange: (LayoutCoordinates) -> Unit
) = composed {
    var lastPosition by remember { mutableStateOf(Offset.Zero) }
    Modifier.onGloballyPositioned { coordinates ->
        if (coordinates.positionInParent != lastPosition) {
            lastPosition = coordinates.positionInParent
            onChange(coordinates)
        }
    }
}

fun Modifier.onPositionInRootChanged(
    onChange: (LayoutCoordinates) -> Unit
) = composed {
    var lastPosition by remember { mutableStateOf(Offset.Zero) }
    Modifier.onGloballyPositioned { coordinates ->
        if (coordinates.positionInRoot() != lastPosition) {
            lastPosition = coordinates.positionInRoot()
            onChange(coordinates)
        }
    }
}
