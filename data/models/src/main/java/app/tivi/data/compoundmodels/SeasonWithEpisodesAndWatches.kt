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

package app.tivi.data.compoundmodels

import androidx.room.Embedded
import androidx.room.Relation
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import java.util.Objects

class SeasonWithEpisodesAndWatches {
    @Embedded
    lateinit var season: Season

    @Relation(parentColumn = "id", entityColumn = "season_id", entity = Episode::class)
    var episodes: List<EpisodeWithWatches> = emptyList()

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is SeasonWithEpisodesAndWatches -> season == other.season && episodes == other.episodes
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(season, episodes)
}

val List<EpisodeWithWatches>.numberAiredToWatch: Int
    get() = count { !it.isWatched && it.episode.hasAired }

val List<EpisodeWithWatches>.numberWatched: Int
    get() = count { it.isWatched }

val List<EpisodeWithWatches>.numberToAir: Int
    get() = size - numberAired

val List<EpisodeWithWatches>.numberAired: Int
    get() = count { it.episode.hasAired }

val List<EpisodeWithWatches>.nextToAir: Episode?
    get() = firstOrNull {
        it.episode.let { ep -> !ep.hasAired && ep.firstAired != null }
    }?.episode
