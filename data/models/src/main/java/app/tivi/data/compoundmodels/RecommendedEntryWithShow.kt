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

package app.tivi.data.compoundmodels

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import app.tivi.data.models.ImageType
import app.tivi.data.models.RecommendedShowEntry
import app.tivi.data.models.ShowTmdbImage
import app.tivi.data.models.TiviShow
import app.tivi.data.util.findHighestRatedItem
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
    val backdrop: ShowTmdbImage? by unsafeLazy {
        findHighestRatedItem(images, ImageType.BACKDROP)
    }

    @delegate:Ignore
    override val poster: ShowTmdbImage? by unsafeLazy {
        findHighestRatedItem(images, ImageType.POSTER)
    }

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is RecommendedEntryWithShow -> {
            entry == other.entry && relations == other.relations && images == other.images
        }
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations, images)
}
