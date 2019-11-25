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

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.navOptions
import app.tivi.common.ui.R

fun NavController.navigateToNavDestination(itemId: Int, popUpToStart: Boolean = true): Boolean {
    val options = navOptions {
        launchSingleTop = true
        anim {
            enter = R.anim.tivi_enter_anim
            exit = R.anim.tivi_exit_anim
            popEnter = R.anim.tivi_pop_enter_anim
            popExit = R.anim.tivi_pop_exit_anim
        }
        if (popUpToStart) {
            popUpTo(graph.findStartDestination().id) {
                inclusive = false
            }
        }
    }

    return try {
        // TODO provide proper API instead of using Exceptions as Control-Flow.
        navigate(itemId, null, options)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}

private fun NavGraph.findStartDestination(): NavDestination {
    var startDestination: NavDestination = this
    while (startDestination is NavGraph) {
        val parent = startDestination
        startDestination = parent.findNode(parent.startDestination)!!
    }
    return startDestination
}
