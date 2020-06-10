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
import androidx.ui.core.LayoutCoordinates
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.drawBackground
import androidx.ui.geometry.Offset
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.preferredHeight
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.PxBounds
import androidx.ui.unit.dp
import androidx.ui.unit.toSize

inline val PxBounds.center: Offset
    get() = Offset((left + right) / 2, (top + bottom) / 2)

inline val LayoutCoordinates.positionInParent: Offset
    get() = parentCoordinates?.childToLocal(this, Offset.Zero) ?: Offset.Zero

inline val LayoutCoordinates.boundsInParent: PxBounds
    get() = PxBounds(positionInParent, size.toSize())

@Composable
fun HorizontalDivider() {
    Box(
        Modifier.preferredHeight(1.dp)
            .fillMaxWidth()
            .drawBackground(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
    )
}
