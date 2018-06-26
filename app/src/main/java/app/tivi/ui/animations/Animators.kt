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
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import androidx.core.animation.doOnEnd
import app.tivi.ui.graphics.TiviColorMatrix

fun saturateDrawableAnimator(current: Drawable, view: View): Animator {
    view.setHasTransientState(true)
    val cm = TiviColorMatrix()

    val animator = ObjectAnimator.ofFloat(cm, TiviColorMatrix.PROP_SATURATION, 0f, 1f)
    animator.duration = 2000L
    animator.interpolator = FastOutSlowInInterpolator()
    animator.addUpdateListener { _ -> current.colorFilter = ColorMatrixColorFilter(cm) }
    animator.doOnEnd {
        current.clearColorFilter()
        view.setHasTransientState(false)
    }

    return animator
}
