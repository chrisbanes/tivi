/*
 * Copyright 2017 Google LLC
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

package app.tivi.ui.transitions

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionValues
import androidx.transition.Visibility

class BabySlide(
    private val gravity: Int = Gravity.START,
    private val babyFraction: Float = 2f
) : Visibility() {

    override fun onAppear(
        sceneRoot: ViewGroup,
        view: View,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator {
        setupTranslation(view)
        view.alpha = 0f

        return ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f)
        )
    }

    override fun onDisappear(
        sceneRoot: ViewGroup,
        view: View,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator {
        return ObjectAnimator.ofPropertyValuesHolder(
            view,
            translatePropValForGravity(view),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0f)
        )
    }

    private fun translatePropValForGravity(view: View): PropertyValuesHolder =
        when (Gravity.getAbsoluteGravity(gravity, view.layoutDirection)) {
            Gravity.TOP -> PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -view.height / babyFraction)
            Gravity.RIGHT -> PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.width / babyFraction)
            Gravity.BOTTOM -> PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.height / babyFraction)
            else -> PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -view.width / babyFraction)
        }

    private fun setupTranslation(view: View) {
        when (Gravity.getAbsoluteGravity(gravity, view.layoutDirection)) {
            Gravity.TOP -> view.translationY = -view.height / babyFraction
            Gravity.RIGHT -> view.translationX = view.width / babyFraction
            Gravity.BOTTOM -> view.translationY = view.width / babyFraction
            else -> view.translationX = -view.width / babyFraction
        }
    }
}
