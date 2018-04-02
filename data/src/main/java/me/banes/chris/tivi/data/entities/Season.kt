/*
 * Copyright 2018 Google, Inc.
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

package me.banes.chris.tivi.data.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.OffsetDateTime
import kotlin.reflect.KMutableProperty0

@Entity(tableName = "seasons",
        indices = [
            Index(value = ["trakt_id"], unique = true),
            Index(value = ["tmdb_id"], unique = true)
        ],
        foreignKeys = [
            ForeignKey(entity = TiviShow::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("show_id"),
                    onUpdate = ForeignKey.CASCADE,
                    onDelete = ForeignKey.CASCADE)
        ])
data class Season(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long? = null,
    @ColumnInfo(name = "show_id") val showId: Long,
    @ColumnInfo(name = "trakt_id") var traktId: Int? = null,
    @ColumnInfo(name = "tmdb_id") var tmdbId: Int? = null,
    @ColumnInfo(name = "title") var title: String? = null,
    @ColumnInfo(name = "overview") var summary: String? = null,
    @ColumnInfo(name = "number") var number: Int = 0,
    @ColumnInfo(name = "ep_count") var episodeCount: Int = 0,
    @ColumnInfo(name = "ep_aired") var airedEpisodes: Int = 0,
    @ColumnInfo(name = "rating") var rating: Float? = null,
    @ColumnInfo(name = "votes") var votes: Int = 0,

    @ColumnInfo(name = "tmdb_poster_path") var tmdbPosterPath: String? = null,
    @ColumnInfo(name = "tmdb_backdrop_path") var tmdbBackdropPath: String? = null,

    @ColumnInfo(name = "trakt_updated") var lastTraktUpdate: OffsetDateTime? = null,
    @ColumnInfo(name = "tmdb_updated") var lastTmdbUpdate: OffsetDateTime? = null
) {
    fun needsUpdateFromTmdb(): Boolean {
        return tmdbId != null && (lastTmdbUpdate?.isBefore(OffsetDateTime.now().minusDays(1)) != false)
    }

    fun needsUpdateFromTrakt(): Boolean {
        return traktId != null && (lastTraktUpdate?.isBefore(OffsetDateTime.now().minusDays(1)) != false)
    }

    fun <T> updateProperty(entityVar: KMutableProperty0<T?>, updateVal: T?) {
        when {
            updateVal != null -> entityVar.set(updateVal)
        }
    }
}
