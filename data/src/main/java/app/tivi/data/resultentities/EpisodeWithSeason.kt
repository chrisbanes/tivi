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
import androidx.room.Relation
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import java.util.Objects

class EpisodeWithSeason {
    @Embedded
    var episode: Episode? = null

    @Relation(parentColumn = "season_id", entityColumn = "id")
    var _seasons: List<Season> = emptyList()

    val season: Season?
        get() = _seasons.getOrNull(0)

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is EpisodeWithSeason -> episode == other.episode && _seasons == other._seasons
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(episode, _seasons)
}
