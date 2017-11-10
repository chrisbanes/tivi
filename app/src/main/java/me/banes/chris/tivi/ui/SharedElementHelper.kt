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

import android.support.v4.app.FragmentTransaction
import android.view.View
import java.lang.ref.WeakReference

class SharedElementHelper {
    private val sharedElementViews = mutableListOf<WeakReference<View>>()
    private val sharedElementNames = mutableListOf<String?>()

    fun addSharedElement(view: View, name: String? = null) {
        sharedElementViews.add(WeakReference(view))
        sharedElementNames.add(name)
    }

    fun apply(tx: FragmentTransaction) {
        for (i in 0 until sharedElementViews.size) {
            sharedElementViews[i].get()?.apply {
                tx.addSharedElement(this, sharedElementNames[i] ?: transitionName)
            }
        }
    }
}
