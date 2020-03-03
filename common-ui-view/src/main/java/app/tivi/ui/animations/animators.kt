/*
 * Copyright 2018 Google LLC
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

package app.tivi.ui.animations

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.util.Property
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import app.tivi.ui.graphics.ImageLoadingColorMatrix
import kotlin.math.roundToLong

private val fastOutSlowInInterpolator = FastOutSlowInInterpolator()

fun saturateDrawableAnimator(
    current: Drawable,
    duration: Long = SATURATION_ANIMATION_DURATION,
    view: View? = null
): Animator {
    current.mutate()
    view?.setHasTransientState(true)

    val cm = ImageLoadingColorMatrix()

    val satAnim = ObjectAnimator.ofFloat(cm, PROP_SATURATION, 0f, 1f)
    satAnim.duration = duration

    satAnim.addUpdateListener {
        current.colorFilter = ColorMatrixColorFilter(cm)
    }

    val alphaAnim = ObjectAnimator.ofFloat(cm, PROP_ALPHA, 0f, 1f)
    alphaAnim.duration = duration / 2

    val darkenAnim = ObjectAnimator.ofFloat(cm, PROP_BRIGHTNESS, 0.8f, 1f)
    darkenAnim.duration = (duration * 0.75f).roundToLong()

    return AnimatorSet().apply {
        playTogether(satAnim, alphaAnim, darkenAnim)
        interpolator = fastOutSlowInInterpolator
        doOnEnd {
            current.clearColorFilter()
            view?.setHasTransientState(false)
        }
    }
}

const val SATURATION_ANIMATION_DURATION = 1000L

private val saturationFloatProp: FloatProp<ImageLoadingColorMatrix> = object : FloatProp<ImageLoadingColorMatrix>("saturation") {
    override operator fun get(o: ImageLoadingColorMatrix): Float = o.saturationFraction
    override operator fun set(o: ImageLoadingColorMatrix, value: Float) {
        o.saturationFraction = value
    }
}

private val alphaFloatProp: FloatProp<ImageLoadingColorMatrix> = object : FloatProp<ImageLoadingColorMatrix>("alpha") {
    override operator fun get(o: ImageLoadingColorMatrix): Float = o.alphaFraction
    override operator fun set(o: ImageLoadingColorMatrix, value: Float) {
        o.alphaFraction = value
    }
}

private val brightnessFloatProp: FloatProp<ImageLoadingColorMatrix> = object : FloatProp<ImageLoadingColorMatrix>("darken") {
    override operator fun get(o: ImageLoadingColorMatrix): Float = o.brightnessFraction
    override operator fun set(o: ImageLoadingColorMatrix, value: Float) {
        o.brightnessFraction = value
    }
}

private val PROP_SATURATION: Property<ImageLoadingColorMatrix, Float> = createFloatProperty(saturationFloatProp)
private val PROP_ALPHA: Property<ImageLoadingColorMatrix, Float> = createFloatProperty(alphaFloatProp)
private val PROP_BRIGHTNESS: Property<ImageLoadingColorMatrix, Float> = createFloatProperty(brightnessFloatProp)
