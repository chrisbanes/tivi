/*
 * Copyright 2019 Google LLC
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
import androidx.compose.remember
import androidx.ui.core.Modifier
import androidx.ui.core.composed
import androidx.ui.core.drawWithContent
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Color
import androidx.ui.graphics.LinearGradientShader
import androidx.ui.graphics.Paint
import androidx.ui.graphics.painter.drawCanvas
import kotlin.math.pow

@Composable
fun Modifier.gradientScrim(
    color: Color,
    numStops: Int = 16
): Modifier = composed {
    val paint = remember {
        Paint().apply {
            isAntiAlias = true
        }
    }
    val colors = remember(color, numStops) {
        val baseAlpha = color.alpha
        List(numStops) { i ->
            val x = i * 1f / (numStops - 1)
            val opacity = x.pow(3.0f)
            color.copy(alpha = baseAlpha * opacity)
        }
    }

    drawWithContent {
        drawContent()

        drawCanvas { canvas, pxSize ->
            paint.shader = LinearGradientShader(
                Offset.zero,
                Offset(0f, pxSize.height.value),
                colors
            )
            canvas.drawRect(0f, 0f, pxSize.width.value, pxSize.height.value, paint)
        }
    }
}
