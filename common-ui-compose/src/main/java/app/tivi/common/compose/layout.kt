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

import androidx.ui.core.LayoutCoordinates
import androidx.ui.core.Modifier
import androidx.ui.layout.absolutePadding
import androidx.ui.unit.Dp
import androidx.ui.unit.PxBounds
import androidx.ui.unit.PxPosition
import androidx.ui.unit.toPxSize

inline val PxBounds.center: PxPosition
    get() = PxPosition((left + right) / 2, (top + bottom) / 2)

inline val LayoutCoordinates.positionInParent: PxPosition
    get() = parentCoordinates?.childToLocal(this, PxPosition.Origin) ?: PxPosition.Origin

inline val LayoutCoordinates.boundsInParent: PxBounds
    get() = PxBounds(positionInParent, size.toPxSize())

fun Modifier.padding(horizontal: Dp, vertical: Dp): Modifier {
    return absolutePadding(left = horizontal, top = vertical, right = horizontal, bottom = vertical)
}
