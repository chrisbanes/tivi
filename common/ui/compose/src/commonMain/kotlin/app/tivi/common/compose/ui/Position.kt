// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

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
import androidx.compose.ui.unit.toSize

inline val LayoutCoordinates.positionInParent: Offset
    get() = parentCoordinates?.localPositionOf(this, Offset.Zero) ?: Offset.Zero

inline val LayoutCoordinates.boundsInParent: Rect
    get() = Rect(positionInParent, size.toSize())

fun Modifier.onPositionInParentChanged(
    onChange: (LayoutCoordinates) -> Unit,
) = composed {
    var lastPosition by remember { mutableStateOf(Offset.Zero) }
    Modifier.onGloballyPositioned { coordinates ->
        if (coordinates.positionInParent != lastPosition) {
            lastPosition = coordinates.positionInParent
            onChange(coordinates)
        }
    }
}
