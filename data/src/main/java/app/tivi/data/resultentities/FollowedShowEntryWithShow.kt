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

package app.tivi.data.resultentities

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.findHighestRatedBackdrop
import app.tivi.data.entities.findHighestRatedPoster
import app.tivi.data.views.FollowedShowsWatchStats
import java.util.Objects

class FollowedShowEntryWithShow : EntryWithShow<FollowedShowEntry> {
    @Embedded
    override lateinit var entry: FollowedShowEntry

    @Relation(parentColumn = "show_id", entityColumn = "id")
    override lateinit var relations: List<TiviShow>

    @Relation(parentColumn = "show_id", entityColumn = "show_id")
    override lateinit var images: List<ShowTmdbImage>

    @Relation(parentColumn = "id", entityColumn = "id")
    lateinit var _stats: List<FollowedShowsWatchStats>

    val stats: FollowedShowsWatchStats?
        get() = _stats.firstOrNull()

    @delegate:Ignore
    val backdrop: ShowTmdbImage? by lazy(LazyThreadSafetyMode.NONE) {
        images.findHighestRatedBackdrop()
    }

    @delegate:Ignore
    override val poster: ShowTmdbImage? by lazy(LazyThreadSafetyMode.NONE) {
        images.findHighestRatedPoster()
    }

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is FollowedShowEntryWithShow -> {
            entry == other.entry && relations == other.relations && stats == other.stats && images == other.images
        }
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(entry, relations, stats, images)
}
