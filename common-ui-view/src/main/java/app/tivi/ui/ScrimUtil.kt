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

package app.tivi.ui

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.util.LruCache
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Utility methods for creating prettier gradient scrims.
 */
object ScrimUtil {
    private val cache = LruCache<Int, Drawable>(10)

    /**
     * Creates an approximated cubic gradient using a multi-stop linear gradient. See
     * [this post](https://plus.google.com/+RomanNurik/posts/2QvHVFWrHZf) for more
     * details.
     */
    fun makeCubicGradientScrimDrawable(
        @ColorInt baseColor: Int,
        numStops: Int,
        gravity: Int
    ): Drawable {
        // Generate a cache key by hashing together the inputs, based on the method described in the Effective Java book
        var cacheKeyHash = baseColor
        cacheKeyHash = 31 * cacheKeyHash + numStops
        cacheKeyHash = 31 * cacheKeyHash + gravity

        val cachedGradient = cache.get(cacheKeyHash)
        if (cachedGradient != null) {
            return cachedGradient
        }

        val paintDrawable = PaintDrawable()
        paintDrawable.shape = RectShape()

        val stopColors = IntArray(numStops)
        val alpha = Color.alpha(baseColor)

        for (i in 0 until numStops) {
            val x = i * 1f / (numStops - 1)
            val opacity = x.toDouble().pow(3.0).toFloat()
            stopColors[i] = ColorUtils.setAlphaComponent(
                baseColor,
                (alpha * opacity).roundToInt().coerceIn(0, 255)
            )
        }

        val x0: Float
        val x1: Float
        val y0: Float
        val y1: Float
        when (gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.LEFT -> {
                x0 = 1f
                x1 = 0f
            }
            Gravity.RIGHT -> {
                x0 = 0f
                x1 = 1f
            }
            else -> {
                x0 = 0f
                x1 = 0f
            }
        }
        when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.TOP -> {
                y0 = 1f
                y1 = 0f
            }
            Gravity.BOTTOM -> {
                y0 = 0f
                y1 = 1f
            }
            else -> {
                y0 = 0f
                y1 = 0f
            }
        }

        paintDrawable.shaderFactory = object : ShapeDrawable.ShaderFactory() {
            override fun resize(width: Int, height: Int): Shader {
                return LinearGradient(
                    width * x0,
                    height * y0,
                    width * x1,
                    height * y1,
                    stopColors,
                    null,
                    Shader.TileMode.CLAMP
                )
            }
        }
        cache.put(cacheKeyHash, paintDrawable)
        return paintDrawable
    }
}
