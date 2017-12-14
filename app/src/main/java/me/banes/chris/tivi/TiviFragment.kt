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

package me.banes.chris.tivi

import android.os.Bundle
import android.support.transition.TransitionInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import me.banes.chris.tivi.extensions.doOnPreDraw

/**
 * Base fragment class which supports LifecycleOwner and Dagger injection.
 */
abstract class TiviFragment : DaggerFragment() {

    private var startedTransition = false
    private var postponed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransitionInflater.from(context).run {
            enterTransition = inflateTransition(R.transition.fragment_enter)
            exitTransition = inflateTransition(R.transition.fragment_exit)
        }
    }

    override fun postponeEnterTransition() {
        super.postponeEnterTransition()
        postponed = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupParentWindowInsetsForTransition()
    }

    override fun onStart() {
        super.onStart()

        if (postponed && !startedTransition) {
            // If we're postponed and haven't started a transition yet, we'll delay for a max of 200ms
            view?.postDelayed(this::scheduleStartPostponedTransitions, 200)
        }
    }

    override fun onStop() {
        super.onStop()
        startedTransition = false
    }

    private fun setupParentWindowInsetsForTransition() {
        val root = view!!
        val container = root.parent as ViewGroup

        val currentSysUiFlags = container.systemUiVisibility

        // Lets declare the container as fullscreen stable so that we get some insets
        // dispatched
        container.systemUiVisibility = currentSysUiFlags or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        container.setOnApplyWindowInsetsListener { view, insets ->
            if (root.isShown) {
                // If the fragment view is now shown, we can revert back to our
                // original sys-ui-vis flags
                view.apply {
                    systemUiVisibility = currentSysUiFlags
                    setOnApplyWindowInsetsListener(null)
                }

                // And make the fragment root declare them instead
                root.apply {
                    systemUiVisibility = systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    requestApplyInsets()
                }
            }

            // Lets manually dispatch them to the fragment root
            root.dispatchApplyWindowInsets(insets)
        }

        // Finally request some insets
        container.requestApplyInsets()
    }

    protected fun scheduleStartPostponedTransitions() {
        if (!startedTransition) {
            (view?.parent as ViewGroup).doOnPreDraw {
                startPostponedEnterTransition()
                activity?.startPostponedEnterTransition()
            }
            startedTransition = true
        }
    }
}