/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.ui.transitions

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.support.transition.TransitionValues
import android.support.transition.Visibility
import android.view.View
import android.view.ViewGroup

class BabySlide : Visibility() {

    override fun onAppear(sceneRoot: ViewGroup, view: View,
            startValues: TransitionValues?,
            endValues: TransitionValues?): Animator {
        view.translationX = -view.width / 2f
        view.alpha = 0f
        return ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f))
    }

    override fun onDisappear(sceneRoot: ViewGroup, view: View, startValues: TransitionValues?, endValues: TransitionValues?): Animator {
        return ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -view.width / 2f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f))
    }

}