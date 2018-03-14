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
import java.util.Objects

interface ListItem<ET : Entry> {
    var entry: ET?
    var relations: List<TiviShow>?

    val show: TiviShow?
        get() = relations?.getOrNull(0)

    fun generateStableId(): Long {
        return Objects.hash(entry!!::class, show!!.id!!).toLong()
    }
}

class TrendingListItem : ListItem<TrendingEntry> {
    @Embedded override var entry: TrendingEntry? = null
    @Relation(parentColumn = "show_id", entityColumn = "id") override var relations: List<TiviShow>? = null

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is TrendingListItem -> entry == other.entry && relations == other.relations
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations)
}

class PopularListItem : ListItem<PopularEntry> {
    @Embedded override var entry: PopularEntry? = null
    @Relation(parentColumn = "show_id", entityColumn = "id") override var relations: List<TiviShow>? = null

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is PopularListItem -> entry == other.entry && relations == other.relations
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations)
}

class WatchedListItem : ListItem<WatchedEntry> {
    @Embedded override var entry: WatchedEntry? = null
    @Relation(parentColumn = "show_id", entityColumn = "id") override var relations: List<TiviShow>? = null

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is WatchedListItem -> entry == other.entry && relations == other.relations
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations)
}

class MyShowsListItem : ListItem<MyShowsEntry> {
    @Embedded override var entry: MyShowsEntry? = null
    @Relation(parentColumn = "show_id", entityColumn = "id") override var relations: List<TiviShow>? = null

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is MyShowsListItem -> entry == other.entry && relations == other.relations
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations)
}