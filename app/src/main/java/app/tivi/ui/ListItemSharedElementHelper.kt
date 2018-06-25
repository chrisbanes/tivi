/*
 * Copyright 2018 Google, Inc.
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

import android.support.v7.widget.RecyclerView
import app.tivi.SharedElementHelper
import app.tivi.data.Entry
import app.tivi.data.entities.EntryWithShow

class ListItemSharedElementHelper(private val recyclerView: RecyclerView) {
    fun createForItem(item: EntryWithShow<out Entry>, transitionName: String): SharedElementHelper {
        val sharedElementHelper = SharedElementHelper()
        addSharedElement(sharedElementHelper, item, transitionName)
        return sharedElementHelper
    }

    fun createForItems(items: List<EntryWithShow<out Entry>>?): SharedElementHelper {
        val sharedElementHelper = SharedElementHelper()
        items?.forEach {
            val homepage = it.show.homepage
            if (homepage != null) {
                addSharedElement(sharedElementHelper, it, homepage)
            }
        }
        return sharedElementHelper
    }

    private fun addSharedElement(
        sharedElementHelper: SharedElementHelper,
        item: EntryWithShow<out Entry>,
        transitionName: String
    ) {
        recyclerView.findViewHolderForItemId(item.generateStableId())?.let {
            sharedElementHelper.addSharedElement(it.itemView, transitionName)
        }
    }
}
