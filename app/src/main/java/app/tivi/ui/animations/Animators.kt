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
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import app.tivi.ui.graphics.ImageLoadingColorMatrix
import kotlin.math.roundToLong

private val fastOutSlowInInterpolator = FastOutSlowInInterpolator()

fun saturateDrawableAnimator(current: Drawable, view: View): Animator {
    view.setHasTransientState(true)
    val cm = ImageLoadingColorMatrix()

    val duration = 1500L

    val satAnim = ObjectAnimator.ofFloat(cm, ImageLoadingColorMatrix.PROP_SATURATION, 0f, 1f)
    satAnim.duration = duration
    satAnim.interpolator = fastOutSlowInInterpolator
    satAnim.addUpdateListener { current.colorFilter = ColorMatrixColorFilter(cm) }

    val alphaAnim = ObjectAnimator.ofFloat(cm, ImageLoadingColorMatrix.PROP_ALPHA, 0f, 1f)
    alphaAnim.duration = duration / 2
    alphaAnim.interpolator = fastOutSlowInInterpolator

    val darkenAnim = ObjectAnimator.ofFloat(cm, ImageLoadingColorMatrix.PROP_DARKEN, 0f, 1f)
    darkenAnim.duration = (duration * 0.75f).roundToLong()
    darkenAnim.interpolator = fastOutSlowInInterpolator

    val set = AnimatorSet()
    set.playTogether(satAnim, alphaAnim, darkenAnim)
    set.doOnEnd {
        current.clearColorFilter()
        view.setHasTransientState(false)
    }
    return set
}
