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

package me.banes.chris.tivi.ui

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.WindowInsetsCompat
import android.util.AttributeSet
import android.view.View

class StatusBarHeightBehavior(context: Context, attrs: AttributeSet?) : CoordinatorLayout.Behavior<View>(context, attrs) {
    override fun onApplyWindowInsets(coordinatorLayout: CoordinatorLayout, child: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val lp = child.layoutParams
        lp.height = insets.systemWindowInsetTop
        child.layoutParams = lp
        return insets.consumeSystemWindowInsets()
    }
}