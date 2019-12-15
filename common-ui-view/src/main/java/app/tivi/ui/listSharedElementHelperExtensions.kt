/*
 * Copyright 2018 Google LLC
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

import android.view.View
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import app.tivi.SharedElementHelper
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow

fun RecyclerView.createSharedElementHelperForItem(
    item: EntryWithShow<out Entry>,
    transitionName: String,
    viewFinder: (view: View) -> View = defaultViewFinder
) = createSharedElementHelperForItemId(item.generateStableId(), transitionName, viewFinder)

fun RecyclerView.createSharedElementHelperForItems(
    items: List<EntryWithShow<out Entry>> = emptyList(),
    viewFinder: (view: View) -> View = defaultViewFinder
): SharedElementHelper {
    val sharedElementHelper = SharedElementHelper()
    items.forEach {
        val homepage = it.show.homepage
        if (homepage != null) {
            sharedElementHelper.addSharedElementFromRecyclerView(this, it.generateStableId(), homepage, viewFinder)
        }
    }
    return sharedElementHelper
}

fun RecyclerView.createSharedElementHelperForItemId(
    viewHolderId: Long,
    transitionName: String,
    viewFinder: (view: View) -> View = defaultViewFinder
) = SharedElementHelper().apply {
    addSharedElementFromRecyclerView(this@createSharedElementHelperForItemId, viewHolderId, transitionName, viewFinder)
}

private fun SharedElementHelper.addSharedElementFromRecyclerView(
    view: RecyclerView,
    viewHolderId: Long,
    transitionName: String,
    viewFinder: (view: View) -> View
): Boolean {
    val itemFromParentRv = view.findViewHolderForItemId(viewHolderId)
    if (itemFromParentRv != null) {
        addSharedElement(viewFinder(itemFromParentRv.itemView), transitionName)
        return true
    }

    // We also check any child RecyclerViews. This is mainly for things like Carousels
    view.forEach { child ->
        if (child is RecyclerView) {
            val itemFromChildRv = child.findViewHolderForItemId(viewHolderId)
            if (itemFromChildRv != null) {
                addSharedElement(viewFinder(itemFromChildRv.itemView), transitionName)
                return true
            }
        }
    }

    return false
}

private val defaultViewFinder: (View) -> View = { it }
