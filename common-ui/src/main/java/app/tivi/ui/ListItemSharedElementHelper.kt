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
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import app.tivi.SharedElementHelper
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow

class ListItemSharedElementHelper(
    private val recyclerView: RecyclerView
) {
    fun createForItem(
        item: EntryWithShow<out Entry>,
        transitionName: String,
        viewFinder: (View) -> View = defaultViewFinder
    ): SharedElementHelper {
        return createForId(item.generateStableId(), transitionName, viewFinder)
    }

    fun createForId(
        viewHolderId: Long,
        transitionName: String,
        viewFinder: (View) -> View = defaultViewFinder
    ): SharedElementHelper {
        val sharedElementHelper = SharedElementHelper()
        addSharedElement(sharedElementHelper, viewHolderId, transitionName, viewFinder)
        return sharedElementHelper
    }

    fun createForItems(
        items: List<EntryWithShow<out Entry>>?,
        viewFinder: (View) -> View = defaultViewFinder
    ): SharedElementHelper {
        val sharedElementHelper = SharedElementHelper()
        items?.forEach {
            val homepage = it.show.homepage
            if (homepage != null) {
                addSharedElement(sharedElementHelper, it.generateStableId(), homepage, viewFinder)
            }
        }
        return sharedElementHelper
    }

    private fun addSharedElement(
        helper: SharedElementHelper,
        viewHolderId: Long,
        transitionName: String,
        viewFinder: (View) -> View
    ): Boolean {
        val itemFromParentRv = recyclerView.findViewHolderForItemId(viewHolderId)
        if (itemFromParentRv != null) {
            helper.addSharedElement(viewFinder(itemFromParentRv.itemView), transitionName)
            return true
        }

        // We also check any child RecyclerViews. This is mainly for things like Carousels
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView[i]
            if (child is RecyclerView) {
                val itemFromChildRv = child.findViewHolderForItemId(viewHolderId)
                if (itemFromChildRv != null) {
                    helper.addSharedElement(viewFinder(itemFromChildRv.itemView), transitionName)
                    return true
                }
            }
        }

        return false
    }

    companion object {
        private val defaultViewFinder: (View) -> View = { it }
    }
}
