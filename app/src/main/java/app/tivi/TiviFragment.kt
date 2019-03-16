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

package app.tivi

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.transition.TransitionInflater
import dagger.android.support.DaggerFragment

/**
 * Base fragment class which supports LifecycleOwner and Dagger injection.
 */
abstract class TiviFragment : DaggerFragment() {
    private var postponedTransition = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransitionInflater.from(context).run {
            enterTransition = inflateTransition(R.transition.fragment_enter)
            exitTransition = inflateTransition(R.transition.fragment_exit)
        }
    }

    override fun postponeEnterTransition() {
        super.postponeEnterTransition()
        postponedTransition = true
    }

    override fun onStart() {
        super.onStart()

        if (postponedTransition) {
            // If we're postponedTransition and haven't started a transition yet, we'll delay for a max of 5 seconds
            view?.postDelayed(::scheduleStartPostponedTransitions, 5000)
        }
    }

    override fun startPostponedEnterTransition() {
        postponedTransition = false
        super.startPostponedEnterTransition()
    }

    protected fun scheduleStartPostponedTransitions() {
        if (postponedTransition) {
            requireView().doOnNextLayout {
                (it.parent as ViewGroup).doOnPreDraw {
                    startPostponedEnterTransition()
                }
            }
            postponedTransition = false
        }

        val activity = activity
        if (activity is TiviActivity) {
            activity.scheduleStartPostponedTransitions()
        }
    }
}
