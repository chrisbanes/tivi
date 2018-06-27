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

package app.tivi.ui.animations

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import androidx.core.animation.doOnEnd
import app.tivi.ui.graphics.ImageLoadingColorMatrix

fun saturateDrawableAnimator(current: Drawable, view: View): Animator {
    view.setHasTransientState(true)
    val cm = ImageLoadingColorMatrix()

    val satPvh = PropertyValuesHolder.ofFloat(ImageLoadingColorMatrix.PROP_SATURATION,0f, 1f)
    val gammaPvh = PropertyValuesHolder.ofFloat(ImageLoadingColorMatrix.PROP_GAMMA, 0f, 1f)

    val animator = ObjectAnimator.ofPropertyValuesHolder(cm, satPvh, gammaPvh)
    animator.duration = 2000L
    animator.interpolator = FastOutSlowInInterpolator()
    animator.addUpdateListener { current.colorFilter = ColorMatrixColorFilter(cm) }
    animator.doOnEnd {
        current.clearColorFilter()
        view.setHasTransientState(false)
    }

    return animator
}
