/*
 * Copyright 2021 Google LLC
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

package app.tivi.home

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit

/**
 * Allows easy hosting of [Fragment] in Compose.
 *
 * @param fragmentKey A unique key for the current fragment.
 * @param enterTransition The transition animation to use when adding the fragment.
 * @param exitTransition The transition animation to use when removing the fragment.
 * @param createFragment Block used for creating the fragment.
 */
@Composable
fun ComposableFragment(
    fragmentKey: Any,
    modifier: Modifier = Modifier,
    enterTransition: Int = FragmentTransaction.TRANSIT_FRAGMENT_FADE,
    exitTransition: Int = FragmentTransaction.TRANSIT_FRAGMENT_FADE,
    createFragment: () -> Fragment,
) {
    val containerId = remember { View.generateViewId() }
    AndroidView<View>(
        factory = ::FragmentContainerView,
        modifier = modifier,
        update = { it.id = containerId }
    )

    // Get the Activity's FragmentManager
    val fragmentManager = (LocalContext.current as FragmentActivity).supportFragmentManager

    DisposableEffect(fragmentKey, fragmentManager) {
        // Create our fragment
        val fragment = createFragment()
        // And add it to our container
        fragmentManager.commit {
            setTransition(enterTransition)
            add(containerId, fragment)
        }

        onDispose {
            if (!fragmentManager.isStateSaved) {
                // If the state isn't saved, that means that some state change
                // has removed this Composable from the hierarchy
                fragmentManager.commit {
                    setTransition(exitTransition)
                    remove(fragment)
                }
            }
        }
    }
}
