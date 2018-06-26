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
 * An extension to [ColorMatrix] which caches the saturation value for animation purposes.
 */
class TiviColorMatrix : ColorMatrix() {
    @Suppress("PropertyName")
    var _saturation = 1f
        private set

    override fun setSaturation(sat: Float) {
        _saturation = sat
        super.setSaturation(sat)
    }

    companion object {
        private val saturationFloatProp = object : FloatProp<TiviColorMatrix>("saturation") {
            override operator fun get(ocm: TiviColorMatrix): Float = ocm._saturation
            override operator fun set(ocm: TiviColorMatrix, saturation: Float) = ocm.setSaturation(saturation)
        }

        val PROP_SATURATION = createFloatProperty(saturationFloatProp)
    }
}