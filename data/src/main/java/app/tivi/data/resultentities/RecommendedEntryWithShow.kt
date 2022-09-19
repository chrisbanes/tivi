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

package app.tivi.data.resultentities

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import app.tivi.data.entities.RecommendedShowEntry
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.findHighestRatedBackdrop
import app.tivi.data.entities.findHighestRatedPoster
import app.tivi.extensions.unsafeLazy
import java.util.Objects

class RecommendedEntryWithShow : EntryWithShow<RecommendedShowEntry> {
    @Embedded
    override lateinit var entry: RecommendedShowEntry

    @Relation(parentColumn = "show_id", entityColumn = "id")
    override lateinit var relations: List<TiviShow>

    @Relation(parentColumn = "show_id", entityColumn = "show_id")
    override lateinit var images: List<ShowTmdbImage>

    @delegate:Ignore
    val backdrop by unsafeLazy { images.findHighestRatedBackdrop() }

    @delegate:Ignore
    override val poster by unsafeLazy { images.findHighestRatedPoster() }

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is RecommendedEntryWithShow -> {
            entry == other.entry && relations == other.relations && images == other.images
        }
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations, images)
}
