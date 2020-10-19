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

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.ElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ln

val AmbientAbsoluteElevation = staticAmbientOf { 0.dp }

/**
 * A wrapper around [Surface] which works with absolute elevation. This will automatically
 * update [AmbientAbsoluteElevation], adding [elevation].
 */
@Composable
fun AbsoluteElevationSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val currentAbsElevation = AmbientAbsoluteElevation.current
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        elevation = elevation
    ) {
        Providers(AmbientAbsoluteElevation provides currentAbsElevation + elevation) {
            content()
        }
    }
}

/**
 * An [ElevationOverlay] which supports absolute elevation, taking the current
 * [AmbientAbsoluteElevation] into account.
 */
object AbsoluteElevationOverlay : ElevationOverlay {
    @Composable
    override fun apply(color: Color, elevation: Dp): Color {
        val colors = MaterialTheme.colors
        val absElevation = AmbientAbsoluteElevation.current + elevation
        return if (absElevation > 0.dp && !colors.isLight) {
            val foregroundColor = calculateOverlayColor(contentColorFor(color), absElevation)
            foregroundColor.compositeOver(color)
        } else color
    }
}

private fun calculateOverlayColor(foregroundColor: Color, elevation: Dp): Color {
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return foregroundColor.copy(alpha = alpha)
}
