/*
 * Copyright 2017 Google, Inc.
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

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import io.sweers.copydynamic.annotations.CopyDynamic
import org.threeten.bp.OffsetDateTime

@CopyDynamic
@Entity(tableName = "shows",
        indices = [
            Index(value = ["trakt_id"], unique = true),
            Index(value = ["tmdb_id"], unique = true)
        ])
data class TiviShow(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") override val id: Long? = null,
    @ColumnInfo(name = "title") val title: String? = null,
    @ColumnInfo(name = "original_title") val originalTitle: String? = null,
    @ColumnInfo(name = "trakt_id") override val traktId: Int? = null,
    @ColumnInfo(name = "tmdb_id") override val tmdbId: Int? = null,
    @ColumnInfo(name = "tmdb_poster_path") val tmdbPosterPath: String? = null,
    @ColumnInfo(name = "tmdb_backdrop_path") val tmdbBackdropPath: String? = null,
    @ColumnInfo(name = "trakt_updated") override val lastTraktUpdate: OffsetDateTime? = null,
    @ColumnInfo(name = "tmdb_updated") override val lastTmdbUpdate: OffsetDateTime? = null,
    @ColumnInfo(name = "overview") val summary: String? = null,
    @ColumnInfo(name = "homepage") val homepage: String? = null,
    @ColumnInfo(name = "rating") val rating: Float? = null,
    @ColumnInfo(name = "certification") val certification: String? = null,
    @ColumnInfo(name = "country") val country: String? = null,
    @ColumnInfo(name = "network") val network: String? = null,
    @ColumnInfo(name = "runtime") val runtime: Int? = null,
    @ColumnInfo(name = "genres") val _genres: String? = null
) : TiviEntity, TraktIdEntity, TmdbIdEntity {
    @Ignore constructor() : this(null)

    fun getGenres() = _genres?.split(",")
            ?.mapNotNull { Genre.fromTraktValue(it.trim()) }
}
