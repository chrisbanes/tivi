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

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.FragmentTransaction
import android.support.v4.util.Pair
import android.view.View
import java.lang.ref.WeakReference

class SharedElementHelper {
    private val sharedElementViews = mutableMapOf<WeakReference<View>, String?>()

    fun addSharedElement(view: View, name: String? = null) {
        sharedElementViews.put(WeakReference(view), name ?: view.transitionName)
    }

    fun applyToTransaction(tx: FragmentTransaction) {
        for ((viewRef, customTransitionName) in sharedElementViews) {
            viewRef.get()?.apply {
                tx.addSharedElement(this, customTransitionName)
            }
        }
    }

    fun applyToIntent(activity: Activity): Bundle? {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                *sharedElementViews.map { Pair(it.key.get(), it.value) }.toList().toTypedArray()
        ).toBundle()
    }

    fun isEmpty(): Boolean = sharedElementViews.isEmpty()
}
