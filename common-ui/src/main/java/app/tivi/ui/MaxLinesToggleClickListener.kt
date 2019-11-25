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

package app.tivi.ui

import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView

class MaxLinesToggleClickListener(private val collapsedLines: Int) : View.OnClickListener {
    private val transition = ChangeBounds().apply {
        duration = 200
        interpolator = FastOutSlowInInterpolator()
    }

    override fun onClick(view: View) {
        TransitionManager.beginDelayedTransition(findParent(view), transition)
        val textView = view as TextView
        textView.maxLines = if (textView.maxLines > collapsedLines) collapsedLines else Int.MAX_VALUE
    }

    private fun findParent(view: View): ViewGroup {
        var parentView: View? = view
        while (parentView != null) {
            val parent = parentView.parent as View?
            if (parent is RecyclerView) {
                return parent
            }
            parentView = parent
        }
        // If we reached here we didn't find a RecyclerView in the parent tree, so lets just use our parent
        return view.parent as ViewGroup
    }
}
