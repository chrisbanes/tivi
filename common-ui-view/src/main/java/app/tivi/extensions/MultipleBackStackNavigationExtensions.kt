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

package app.tivi.extensions

import android.content.Intent
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import app.tivi.common.ui.R

class MultipleBackStackNavigation(
    private val navGraphIds: List<Int>,
    private val fragmentManager: FragmentManager,
    private val containerId: Int,
    private val intent: Intent,
    private val getSelectedItemId: () -> Int,
    private val setSelectedItemId: (Int) -> Unit,
) {
    // Map of tags
    private val graphIdToTagMap = SparseArray<String>()

    // Result. Mutable live data with the selected controlled
    private val _selectedNavController = MutableLiveData<NavController>()

    val selectedNavController: LiveData<NavController>
        get() = _selectedNavController

    private var firstFragmentGraphId = 0
    private val firstFragmentTag: String

    // Now connect selecting an item with swapping Fragments
    private var selectedItemTag: String
    private var isOnFirstFragment: Boolean

    init {
        // First create a NavHostFragment for each NavGraph ID
        navGraphIds.forEachIndexed { index, navGraphId ->
            val fragmentTag = getFragmentTag(index)

            // Find or create the Navigation host fragment
            val navHostFragment = obtainNavHostFragment(
                fragmentManager,
                fragmentTag,
                navGraphId,
                containerId
            )

            // Obtain its id
            val graphId = navHostFragment.navController.graph.id

            if (index == 0) firstFragmentGraphId = graphId

            // Save to the map
            graphIdToTagMap[graphId] = fragmentTag

            // Attach or detach nav host fragment depending on whether it's the selected item.
            if (getSelectedItemId() == graphId) {
                // Update livedata with the selected graph
                _selectedNavController.value = navHostFragment.navController
                attachNavHostFragment(fragmentManager, navHostFragment, index == 0)
            } else {
                detachNavHostFragment(fragmentManager, navHostFragment)
            }
        }

        selectedItemTag = graphIdToTagMap[getSelectedItemId()]
        firstFragmentTag = graphIdToTagMap[firstFragmentGraphId]
        isOnFirstFragment = selectedItemTag == firstFragmentTag

        // Finally, ensure that we update our BottomNavigationView when the back stack changes
        fragmentManager.addOnBackStackChangedListener {
            if (!isOnFirstFragment && !fragmentManager.isOnBackStack(firstFragmentTag)) {
                setSelectedItemId(firstFragmentGraphId)
            }

            // Reset the graph if the currentDestination is not valid (happens when the back
            // stack is popped after using the back button).
            selectedNavController.value?.let { controller ->
                if (controller.currentDestination == null) {
                    controller.navigate(controller.graph.id)
                }
            }
        }

        setupDeepLinks()
    }

    fun onItemSelected(itemId: Int): Boolean {
        // Don't do anything if the state is state has already been saved.
        if (fragmentManager.isStateSaved) return false

        val newlySelectedItemTag = graphIdToTagMap[itemId]
        if (selectedItemTag == newlySelectedItemTag) return false

        // Pop everything above the first fragment (the "fixed start destination")
        fragmentManager.popBackStack(
            firstFragmentTag,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
            as NavHostFragment

        // Exclude the first fragment tag because it's always in the back stack.
        if (firstFragmentTag != newlySelectedItemTag) {
            // Commit a transaction that cleans the back stack and adds the first fragment
            // to it, creating the fixed started destination.
            fragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.tivi_enter_anim,
                    R.anim.tivi_exit_anim,
                    R.anim.tivi_pop_enter_anim,
                    R.anim.tivi_pop_exit_anim
                )
                .attach(selectedFragment)
                .setPrimaryNavigationFragment(selectedFragment)
                .apply {
                    // Detach all other Fragments
                    graphIdToTagMap.forEach { _, fragmentTagIter ->
                        if (fragmentTagIter != newlySelectedItemTag) {
                            detach(fragmentManager.findFragmentByTag(firstFragmentTag)!!)
                        }
                    }
                }
                .addToBackStack(firstFragmentTag)
                .setReorderingAllowed(true)
                .commit()
        }
        selectedItemTag = newlySelectedItemTag
        isOnFirstFragment = selectedItemTag == firstFragmentTag
        _selectedNavController.value = selectedFragment.navController

        return true
    }

    fun onReselected(itemId: Int) {
        val newlySelectedItemTag = graphIdToTagMap[itemId]
        val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
            as NavHostFragment
        val navController = selectedFragment.navController
        // Pop the back stack to the start destination of the current navController graph
        navController.popBackStack(
            navController.graph.startDestination,
            false
        )
    }

    private fun setupDeepLinks() {
        navGraphIds.forEachIndexed { index, navGraphId ->
            val fragmentTag = getFragmentTag(index)

            // Find or create the Navigation host fragment
            val navHostFragment = obtainNavHostFragment(
                fragmentManager,
                fragmentTag,
                navGraphId,
                containerId
            )
            // Handle Intent
            if (navHostFragment.navController.handleDeepLink(intent)) {
                setSelectedItemId(navHostFragment.navController.graph.id)
            }
        }
    }
}

private fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment
) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

private fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
    isPrimaryNavFragment: Boolean
) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
        .commitNow()
}

private fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    navGraphId: Int,
    containerId: Int
): NavHostFragment {
    // If the Nav Host fragment exists, return it
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    existingFragment?.let { return it }

    // Otherwise, create it and return it.
    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

private fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    val backStackCount = backStackEntryCount
    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}

private fun getFragmentTag(index: Int) = "bottomNavigation#$index"
