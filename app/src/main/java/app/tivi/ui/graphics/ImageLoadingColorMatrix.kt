/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.ui.graphics

import android.graphics.ColorMatrix
import app.tivi.ui.animations.FloatProp
import app.tivi.ui.animations.createFloatProperty

/**
 * An extension to [ColorMatrix] which implements the Material image loading pattern
 */
class ImageLoadingColorMatrix : ColorMatrix() {
    private val elements = FloatArray(20)

    var saturationFraction = 1f
        set(value) {
            System.arraycopy(array, 0, elements, 0, 20)

            // Desaturate over [0, 1], taken from ColorMatrix.setSaturation
            val invSat = 1 - value
            val r = 0.213f * invSat
            val g = 0.715f * invSat
            val b = 0.072f * invSat

            elements[0] = r + value
            elements[1] = g
            elements[2] = b
            elements[5] = r
            elements[6] = g + value
            elements[7] = b
            elements[10] = r
            elements[11] = g
            elements[12] = b + value

            set(elements)
        }

    var alphaFraction = 1f
        set(value) {
            System.arraycopy(array, 0, elements, 0, 20)

            // We phase this over the first 2/3 of the window
            val phaseMax = 2 / 3f
            // Compute the alpha change over period [0, 0.66667]
            elements[18] = value.coerceAtMost(phaseMax) / phaseMax

            set(elements)
        }

    var gammaFraction = 1f
        set(value) {
            System.arraycopy(array, 0, elements, 0, 20)

            // The gamma is phased over the first [0, 0.8333]
            val phaseMax = 5 / 6f
            val invValue = 1 - (value.coerceAtMost(phaseMax) / phaseMax)

            // We substract to make the picture look darker, it will automatically clamp
            val blackening = invValue * MAX_GAMMA_SUBTRACTION * 255
            elements[4] = -blackening
            elements[9] = -blackening
            elements[14] = -blackening

            set(elements)
        }

    companion object {
        private val saturationFloatProp = object : FloatProp<ImageLoadingColorMatrix>("saturation") {
            override operator fun get(o: ImageLoadingColorMatrix): Float = o.saturationFraction
            override operator fun set(o: ImageLoadingColorMatrix, value: Float) {
                o.saturationFraction = value
            }
        }

        private val alphaFloatProp = object : FloatProp<ImageLoadingColorMatrix>("alpha") {
            override operator fun get(o: ImageLoadingColorMatrix): Float = o.alphaFraction
            override operator fun set(o: ImageLoadingColorMatrix, value: Float) {
                o.alphaFraction = value
            }
        }

        private val gammaFloatProp = object : FloatProp<ImageLoadingColorMatrix>("gamma") {
            override operator fun get(o: ImageLoadingColorMatrix): Float = o.gammaFraction
            override operator fun set(o: ImageLoadingColorMatrix, value: Float) {
                o.gammaFraction = value
            }
        }

        val PROP_SATURATION = createFloatProperty(saturationFloatProp)
        val PROP_ALPHA = createFloatProperty(alphaFloatProp)
        val PROP_GAMMA = createFloatProperty(gammaFloatProp)

        // This means that we darken the image by 20%
        private const val MAX_GAMMA_SUBTRACTION = 0.20f
    }
}