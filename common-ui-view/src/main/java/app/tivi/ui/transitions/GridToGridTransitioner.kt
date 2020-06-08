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

package app.tivi.ui.transitions

import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionSet

object GridToGridTransitioner {

    // If you wish to
    private const val CONTENT_TRANSITION_DURATION = 200L

    private const val RETURN_TRANSITION_FRACTION = 2 / 3f
    private const val GRID_SHARED_ELEMENT_ENTER_DURATION = CONTENT_TRANSITION_DURATION * 4 / 3
    private const val GRID_SHARED_ELEMENT_RETURN_DURATION =
        (GRID_SHARED_ELEMENT_ENTER_DURATION * RETURN_TRANSITION_FRACTION).toLong()

    private val LINEAR_INTERPOLATOR = LinearInterpolator()
    private val FAST_OUT_SLOW_IN_INTERPOLATOR = FastOutSlowInInterpolator()
    private val FAST_OUT_LINEAR_IN_INTERPOLATOR = FastOutLinearInInterpolator()
    private val LINEAR_OUT_SLOW_IN_INTERPOLATOR = LinearOutSlowInInterpolator()

    /**
     * Setup the first fragment.
     *
     * @param fragment The fragment to setup
     * @param contentFadeElementIds An array of view ids which will transition at the normal rate. This will
     * typically be your toolbar
     */
    fun setupFirstFragment(fragment: Fragment, vararg contentFadeElementIds: Int) {
        fragment.exitTransition = TransitionSet().apply {
            addTransition(
                Fade().apply {
                    contentFadeElementIds.forEach { excludeTarget(it, true) }
                    interpolator = LINEAR_INTERPOLATOR
                    duration = CONTENT_TRANSITION_DURATION / 2
                }
            )
            addTransition(
                Fade().apply {
                    contentFadeElementIds.forEach { addTarget(it) }
                    interpolator = LINEAR_INTERPOLATOR
                    duration = CONTENT_TRANSITION_DURATION
                }
            )
        }
        fragment.reenterTransition = Fade().apply {
            interpolator = LINEAR_INTERPOLATOR
            duration = CONTENT_TRANSITION_DURATION
        }
    }

    /**
     * Setup the second fragment.
     *
     * @param fragment The fragment to setup
     * @param fadeElementIds An array of view ids which will fade rather than slide. This will typically be your toolbar
     */
    fun setupSecondFragment(
        fragment: Fragment,
        vararg fadeElementIds: Int,
        onTransitionEnd: () -> Unit
    ) {
        fragment.sharedElementEnterTransition = ColumnedChangeBounds().apply {
            interpolator = FAST_OUT_SLOW_IN_INTERPOLATOR
            duration = GRID_SHARED_ELEMENT_ENTER_DURATION
        }
        fragment.sharedElementReturnTransition = ColumnedChangeBounds().apply {
            interpolator = FAST_OUT_SLOW_IN_INTERPOLATOR
            duration = GRID_SHARED_ELEMENT_RETURN_DURATION
        }
        fragment.enterTransition = TransitionSet().apply {
            addTransition(
                Fade().apply {
                    fadeElementIds.forEach { addTarget(it) }
                    interpolator = LINEAR_INTERPOLATOR
                }
            )
            addTransition(
                BabySlide().apply {
                    fadeElementIds.forEach { excludeTarget(it, true) }
                    interpolator = LINEAR_OUT_SLOW_IN_INTERPOLATOR
                }
            )
            duration = CONTENT_TRANSITION_DURATION
            // We want this to end at the same time as the shared element transition so we delay it
            startDelay = GRID_SHARED_ELEMENT_ENTER_DURATION - CONTENT_TRANSITION_DURATION

            addListener(object : TransitionListenerAdapter() {
                override fun onTransitionEnd(transition: Transition) {
                    onTransitionEnd()
                }
            })
        }
        fragment.returnTransition = TransitionSet().apply {
            addTransition(
                Fade().apply {
                    fadeElementIds.forEach { addTarget(it) }
                    interpolator = LINEAR_INTERPOLATOR
                }
            )
            addTransition(
                BabySlide().apply {
                    fadeElementIds.forEach { excludeTarget(it, true) }
                    interpolator = FAST_OUT_LINEAR_IN_INTERPOLATOR
                }
            )
            duration = (CONTENT_TRANSITION_DURATION * RETURN_TRANSITION_FRACTION).toLong()
        }
    }
}
