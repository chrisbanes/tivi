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

package app.tivi.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import android.view.View
import android.view.ViewGroup
import org.hamcrest.Matcher

fun rotateLandscape(): ViewAction {
    return OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
}

fun rotatePortrait(): ViewAction {
    return OrientationChangeAction(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
}

private class OrientationChangeAction(private val orientation: Int) : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isRoot()
    }

    override fun getDescription(): String {
        return "Rotating to $orientation"
    }

    override fun perform(uiController: UiController, view: View) {
        uiController.loopMainThreadUntilIdle()
        var activity = getActivity(view.context)

        if (activity == null && view is ViewGroup) {
            val c = view.childCount
            var i = 0
            while (i < c && activity == null) {
                activity = getActivity(view.getChildAt(i).context)
                ++i
            }
        }
        activity!!.requestedOrientation = orientation

        uiController.loopMainThreadUntilIdle()
    }

    private fun getActivity(context: Context): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = (context).baseContext
        }
        return null
    }
}