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

package app.tivi

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.transition.TransitionInflater
import app.tivi.common.ui.R
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Base fragment class which supports LifecycleOwner and Dagger injection.
 */
abstract class TiviFragment : DaggerFragment() {
    /**
     * This is to force Dagger to create an injector in this module. Otherwise all of the
     * dependent modules create one, which leads to duplicate classes, which breaks R8.
     */
    @Suppress("unused")
    @Inject
    lateinit var _dummyTiviFragment: Context

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
