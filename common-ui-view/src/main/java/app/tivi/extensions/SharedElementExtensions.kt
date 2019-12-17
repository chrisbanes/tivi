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

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import app.tivi.SharedElementHelper

fun SharedElementHelper.toBundle(activity: Activity): Bundle? {
    return toActivityOptions(activity)?.toBundle()
}

fun SharedElementHelper.toActivityOptions(activity: Activity): ActivityOptionsCompat? {
    if (!isEmpty()) {
        return ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity,
            *sharedElements.map { Pair(it.key, it.value) }.toTypedArray()
        )
    }
    return null
}

fun SharedElementHelper.applyToTransaction(tx: FragmentTransaction) {
    for ((view, customTransitionName) in sharedElements) {
        tx.addSharedElement(view, customTransitionName!!)
    }
}

fun SharedElementHelper.toFragmentNavigatorExtras(): Navigator.Extras {
    return FragmentNavigator.Extras.Builder()
        .addSharedElements(sharedElements)
        .build()
}

fun SharedElementHelper.toActivityNavigatorExtras(activity: Activity): Navigator.Extras {
    return ActivityNavigatorExtras(toActivityOptions(activity))
}

fun sharedElementHelperOf(vararg elements: kotlin.Pair<View, String>) = SharedElementHelper().apply {
    elements.forEach { (view, transitionName) -> addSharedElement(view, transitionName) }
}
