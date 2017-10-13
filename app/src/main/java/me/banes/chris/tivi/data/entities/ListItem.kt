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

package me.banes.chris.tivi.data.entities

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import me.banes.chris.tivi.data.Entry

interface ListItem<ET : Entry> {
    var entry: ET?
    var shows: MutableList<TiviShow>?
}

class TrendingListItem : ListItem<TrendingEntry> {
    @Embedded
    override var entry: TrendingEntry? = null

    @Relation(parentColumn = "show_id", entityColumn = "id", entity = TiviShow::class)
    override var shows: MutableList<TiviShow>? = null
}

class PopularListItem : ListItem<PopularEntry> {
    @Embedded
    override var entry: PopularEntry? = null

    @Relation(parentColumn = "show_id", entityColumn = "id", entity = TiviShow::class)
    override var shows: MutableList<TiviShow>? = null
}

class WatchedListItem : ListItem<WatchedEntry> {
    @Embedded
    override var entry: WatchedEntry? = null

    @Relation(parentColumn = "show_id", entityColumn = "id", entity = TiviShow::class)
    override var shows: MutableList<TiviShow>? = null
}