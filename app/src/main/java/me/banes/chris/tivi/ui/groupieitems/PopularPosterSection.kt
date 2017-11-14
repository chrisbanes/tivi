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

package me.banes.chris.tivi.ui.groupieitems

import com.xwray.groupie.Section
import com.xwray.groupie.UpdatingGroup
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.data.entities.PopularEntry
import me.banes.chris.tivi.data.entities.TrendingEntry

internal class PopularPosterSection(items: List<ListItem<PopularEntry>>) : Section() {
    private val group = UpdatingGroup()

    init {
        add(group)
        update(items)
    }

    fun update(items: List<ListItem<PopularEntry>>) {
        group.update(items.map { PopularPosterItem(it.entry!!, it.show!!) })
    }
}