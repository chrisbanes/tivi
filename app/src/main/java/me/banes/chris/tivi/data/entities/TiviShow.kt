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

package me.banes.chris.tivi.data.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import java.util.Date
import java.util.concurrent.TimeUnit

@Entity(tableName = "shows",
        indices = arrayOf(
                Index(value = "trakt_id", unique = true),
                Index(value = "tmdb_id", unique = true)))
data class TiviShow(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long? = null,
        @ColumnInfo(name = "title") val title: String,
        @ColumnInfo(name = "original_title") val originalTitle: String? = null,
        @ColumnInfo(name = "trakt_id") val traktId: Int? = null,
        @ColumnInfo(name = "tmdb_id") val tmdbId: Int? = null,
        @ColumnInfo(name = "tmdb_poster_path") val tmdbPosterPath: String? = null,
        @ColumnInfo(name = "tmdb_backdrop_path") val tmdbBackdropPath: String? = null,
        @ColumnInfo(name = "trakt_updated") val lastTraktUpdate: Date? = null,
        @ColumnInfo(name = "tmdb_updated") val lastTmdbUpdate: Date? = null,
        @ColumnInfo(name = "overview") val summary: String? = null,
        @ColumnInfo(name = "homepage") val homepage: String? = null) {

    fun needsUpdateFromTmdb(): Boolean {
        return tmdbId == null
                || lastTmdbUpdate == null
                || olderThan(lastTmdbUpdate, 1, TimeUnit.DAYS)
    }

    private fun olderThan(date: Date, period: Long, unit: TimeUnit): Boolean {
        return date.time < System.currentTimeMillis() - unit.toMillis(period)
    }
}

