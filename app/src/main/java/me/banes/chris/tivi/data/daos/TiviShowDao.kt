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

package me.banes.chris.tivi.data.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import io.reactivex.Maybe
import me.banes.chris.tivi.data.entities.TiviShow

@Dao
interface TiviShowDao {
    @Query("SELECT * FROM shows WHERE trakt_id = :id")
    fun getShowWithTraktId(id: Int): Maybe<TiviShow>

    @Query("SELECT * FROM shows WHERE tmdb_id = :id")
    fun getShowWithTmdbId(id: Int): Maybe<TiviShow>

    @Query("SELECT * FROM shows WHERE id IN (:ids)")
    fun getShowWithIds(ids: List<Long>): List<TiviShow>

    @Query("SELECT * FROM shows WHERE " +
            "(tmdb_id IS NOT NULL AND tmdb_id = :tmdbId) OR " +
            "(trakt_id IS NOT NULL AND trakt_id = :traktId)")
    fun getShowFromId(tmdbId: Int? = null, traktId: Int? = null): Maybe<TiviShow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertShow(shows: TiviShow): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateShow(shows: TiviShow)
}
