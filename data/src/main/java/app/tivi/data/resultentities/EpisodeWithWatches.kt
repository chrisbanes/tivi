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
import androidx.room.Relation
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import org.threeten.bp.OffsetDateTime
import java.util.Objects

class EpisodeWithWatches {
    @Embedded
    var episode: Episode? = null

    @Relation(parentColumn = "id", entityColumn = "episode_id")
    var watches: List<EpisodeWatchEntry> = emptyList()

    fun hasWatches() = watches.isNotEmpty()

    fun isWatched() = watches.any {
        it.pendingAction != PendingAction.DELETE
    }

    fun hasPending() = watches.any {
        it.pendingAction != PendingAction.NOTHING
    }

    fun onlyPendingDeletes() = watches.all {
        it.pendingAction == PendingAction.DELETE
    }

    fun hasAired(): Boolean {
        val aired = episode?.firstAired
        return aired == null || aired.isBefore(OffsetDateTime.now())
    }

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is EpisodeWithWatches -> watches == other.watches && episode == other.episode
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(episode, watches)
}