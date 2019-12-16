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

package app.tivi.episodedetails

import androidx.compose.Composable
import androidx.compose.memo
import androidx.compose.unaryPlus
import androidx.ui.core.Draw
import androidx.ui.core.toRect
import androidx.ui.engine.geometry.Offset
import androidx.ui.graphics.Color
import androidx.ui.graphics.LinearGradientShader
import androidx.ui.graphics.Paint
import kotlin.math.pow

@Composable
fun GradientScrim(baseColor: Color, numStops: Int = 16) {
    val paint = +memo { Paint() }

    Draw { canvas, parentSize ->
        val alpha = baseColor.alpha
        val colors = List(numStops) { i ->
            val x = i * 1f / (numStops - 1)
            val opacity = x.toDouble().pow(3.0).toFloat()
            baseColor.copy(alpha = alpha * opacity)
        }

        paint.shader = LinearGradientShader(
                Offset.zero,
                Offset(0f, parentSize.height.value),
                colors
        )

        canvas.drawRect(parentSize.toRect(), paint)
    }
}
