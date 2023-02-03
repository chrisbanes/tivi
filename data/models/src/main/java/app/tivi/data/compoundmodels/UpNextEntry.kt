/*
 * Copyright 2023 Google LLC
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
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import app.tivi.data.models.TiviShow
import app.tivi.data.views.FollowedShowsNextToWatch
import app.tivi.extensions.unsafeLazy

class UpNextEntry {
    @Embedded
    lateinit var entity: FollowedShowsNextToWatch

    @Relation(parentColumn = "show_id", entityColumn = "id")
    lateinit var _show: List<TiviShow>

    @delegate:Ignore
    val show: TiviShow by unsafeLazy { _show.first() }

    @Relation(parentColumn = "season_id", entityColumn = "id")
    lateinit var _season: List<Season>

    @delegate:Ignore
    val season: Season by unsafeLazy { _season.first() }

    @Relation(parentColumn = "episode_id", entityColumn = "id")
    lateinit var _episode: List<Episode>

    @delegate:Ignore
    val episode: Episode by unsafeLazy { _episode.first() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpNextEntry

        if (entity != other.entity) return false
        if (_show != other._show) return false
        if (_season != other._season) return false
        if (_episode != other._episode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entity.hashCode()
        result = 31 * result + _show.hashCode()
        result = 31 * result + _season.hashCode()
        result = 31 * result + _episode.hashCode()
        return result
    }
}
