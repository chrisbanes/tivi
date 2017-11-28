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

package me.banes.chris.tivi.home.discover

import android.support.v7.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import me.banes.chris.tivi.R
import me.banes.chris.tivi.SharedElementHelper
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.extensions.filterItems
import me.banes.chris.tivi.ui.groupieitems.EmptyPlaceholderItem
import me.banes.chris.tivi.ui.groupieitems.HeaderItem
import me.banes.chris.tivi.ui.groupieitems.ShowPosterItem

class SectionedHelper<S>(
        private val recyclerView: RecyclerView,
        private val adapter: GroupAdapter<ViewHolder>,
        private val spanCount: Int,
        private val sectionGroupMapper: (S, List<ListItem<out Entry>>) -> Section,
        private val titleFetcher: (S) -> String
) {
    private val sectionMap = mutableMapOf<S, Section>()

    fun update(data: Map<S, List<ListItem<out Entry>>>) {
        adapter.clear()
        sectionMap.clear()

        data.forEach {
            val key = it.key
            sectionGroupMapper(key, it.value.filter { it.show != null }.take(spanCount * 2))
                    .run {
                        setHeader(HeaderItem(titleFetcher(key), key))
                        setPlaceholder(EmptyPlaceholderItem())
                        adapter.add(this)
                        sectionMap[key] = this
                    }
        }
    }

    fun addSharedElementsForSection(section: S, sharedElements: SharedElementHelper) {
        sectionMap[section]!!
                .filterItems { it.layout == R.layout.grid_item }
                .forEach {
                    if (it is ShowPosterItem) {
                        addSharedElementForItem(it, sharedElements)
                    }
                }
    }

    fun addSharedElementForItem(
            item: ShowPosterItem,
            sharedElements: SharedElementHelper,
            name: String? = item.show.homepage) {
        val adapterPos = adapter.getAdapterPosition(item)
        val vh = recyclerView.findViewHolderForAdapterPosition(adapterPos)
        sharedElements.addSharedElement(vh.itemView, name)
    }
}