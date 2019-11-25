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
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import java.util.Objects

class SeasonWithShow {
    @Embedded
    lateinit var season: Season

    @Relation(parentColumn = "show_id", entityColumn = "id")
    var _shows: List<TiviShow> = emptyList()

    val show: TiviShow
        get() {
            assert(_shows.size == 1)
            return _shows[0]
        }

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other is SeasonWithShow -> season == other.season && _shows == other._shows
        else -> false
    }

    override fun hashCode(): Int = Objects.hash(season, _shows)
}
