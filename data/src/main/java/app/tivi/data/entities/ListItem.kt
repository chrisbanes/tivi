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

package app.tivi.data.entities

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import app.tivi.data.Entry
import java.util.Objects

interface ListItem<ET : Entry> {
    var entry: ET?
    var relations: List<TiviShow>

    val show: TiviShow
        get() {
            assert(relations.size == 1)
            return relations[0]
        }

    fun generateStableId(): Long {
        return Objects.hash(entry!!::class, show.id!!).toLong()
    }
}

class TrendingListItem : ListItem<TrendingEntry> {
    @Embedded override var entry: TrendingEntry? = null
    @Relation(parentColumn = "show_id", entityColumn = "id") override var relations: List<TiviShow> = emptyList()

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is TrendingListItem -> entry == other.entry && relations == other.relations
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations)
}

class PopularListItem : ListItem<PopularEntry> {
    @Embedded override var entry: PopularEntry? = null
    @Relation(parentColumn = "show_id", entityColumn = "id") override var relations: List<TiviShow> = emptyList()

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is PopularListItem -> entry == other.entry && relations == other.relations
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations)
}

class WatchedShowListItem : ListItem<WatchedShowEntry> {
    @Embedded override var entry: WatchedShowEntry? = null
    @Relation(parentColumn = "show_id", entityColumn = "id") override var relations: List<TiviShow> = emptyList()

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is WatchedShowListItem -> entry == other.entry && relations == other.relations
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations)
}

class FollowedShowsListItem : ListItem<FollowedShowEntry> {
    @Embedded override var entry: FollowedShowEntry? = null
    @Relation(parentColumn = "show_id", entityColumn = "id") override var relations: List<TiviShow> = emptyList()

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is FollowedShowsListItem -> entry == other.entry && relations == other.relations
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations)
}

class RelatedShowsListItem : ListItem<RelatedShowEntry> {
    @Embedded override var entry: RelatedShowEntry? = null
    @Relation(parentColumn = "other_show_id", entityColumn = "id") override var relations: List<TiviShow> = emptyList()

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is RelatedShowsListItem -> entry == other.entry && relations == other.relations
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations)
}