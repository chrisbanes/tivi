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

package app.tivi.data.resultentities

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import app.tivi.data.entities.PopularShowEntry
import app.tivi.data.entities.TiviShow
import java.util.Objects

class PopularEntryWithShow : EntryWithShow<PopularShowEntry> {
    @Embedded
    override var entry: PopularShowEntry? = null
    @Relation(parentColumn = "show_id", entityColumn = "id") override var relations: List<TiviShow> = emptyList()

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is PopularEntryWithShow -> entry == other.entry && relations == other.relations
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations)
}