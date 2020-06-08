/*
 * Copyright 2017 Google LLC
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

package app.tivi.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId

@Entity(
    tableName = "shows",
    indices = [
        Index(value = ["trakt_id"], unique = true),
        Index(value = ["tmdb_id"])
    ]
)
data class TiviShow(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") override val id: Long = 0,
    @ColumnInfo(name = "title") val title: String? = null,
    @ColumnInfo(name = "original_title") val originalTitle: String? = null,
    @ColumnInfo(name = "trakt_id") override val traktId: Int? = null,
    @ColumnInfo(name = "tmdb_id") override val tmdbId: Int? = null,
    @ColumnInfo(name = "imdb_id") val imdbId: String? = null,
    @ColumnInfo(name = "overview") val summary: String? = null,
    @ColumnInfo(name = "homepage") val homepage: String? = null,
    @ColumnInfo(name = "trakt_rating") val traktRating: Float? = null,
    @ColumnInfo(name = "trakt_votes") val traktVotes: Int? = null,
    @ColumnInfo(name = "certification") val certification: String? = null,
    @ColumnInfo(name = "first_aired") val firstAired: OffsetDateTime? = null,
    @ColumnInfo(name = "country") val country: String? = null,
    @ColumnInfo(name = "network") val network: String? = null,
    @ColumnInfo(name = "network_logo_path") val networkLogoPath: String? = null,
    @ColumnInfo(name = "runtime") val runtime: Int? = null,
    @ColumnInfo(name = "genres") val _genres: String? = null,
    @ColumnInfo(name = "last_trakt_data_update") val traktDataUpdate: OffsetDateTime? = null,
    @ColumnInfo(name = "status") val status: ShowStatus? = null,
    @ColumnInfo(name = "airs_day") val airsDay: DayOfWeek? = null,
    @ColumnInfo(name = "airs_time") val airsTime: LocalTime? = null,
    @ColumnInfo(name = "airs_tz") val airsTimeZone: ZoneId? = null
) : TiviEntity, TraktIdEntity, TmdbIdEntity {
    @Ignore
    constructor() : this(0)

    @delegate:Ignore
    val genres by lazy(LazyThreadSafetyMode.NONE) {
        _genres?.split(",")?.mapNotNull { Genre.fromTraktValue(it.trim()) } ?: emptyList()
    }

    companion object {
        val EMPTY_SHOW = TiviShow()
    }
}
