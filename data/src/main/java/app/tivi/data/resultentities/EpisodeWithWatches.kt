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
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import org.threeten.bp.OffsetDateTime
import java.util.Objects

class EpisodeWithWatches {
    @Embedded
    lateinit var episode: Episode

    @Relation(parentColumn = "id", entityColumn = "episode_id")
    lateinit var watches: List<EpisodeWatchEntry>

    @delegate:Ignore
    val hasWatches by lazy { watches.isNotEmpty() }

    @delegate:Ignore
    val isWatched by lazy {
        watches.any { it.pendingAction != PendingAction.DELETE }
    }

    @delegate:Ignore
    val hasPending by lazy {
        watches.any { it.pendingAction != PendingAction.NOTHING }
    }

    @delegate:Ignore
    val onlyPendingDeletes by lazy {
        watches.all { it.pendingAction == PendingAction.DELETE }
    }

    @delegate:Ignore
    val hasAired by lazy {
        val aired = episode.firstAired
        aired != null && aired.isBefore(OffsetDateTime.now())
    }

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is EpisodeWithWatches -> watches == other.watches && episode == other.episode
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(episode, watches)
}
