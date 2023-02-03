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

package app.tivi.data.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.tivi.data.compoundmodels.ShowDetailed
import app.tivi.data.models.TiviShow
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RoomTiviShowDao : TiviShowDao, RoomEntityDao<TiviShow> {
    @Query("SELECT * FROM shows WHERE trakt_id = :id")
    abstract override suspend fun getShowWithTraktId(id: Int): TiviShow?

    @Query("SELECT * FROM shows WHERE id IN (:ids)")
    abstract override fun getShowsWithIds(ids: List<Long>): Flow<List<TiviShow>>

    @Query("SELECT * FROM shows WHERE tmdb_id = :id")
    abstract override suspend fun getShowWithTmdbId(id: Int): TiviShow?

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract override fun getShowWithIdFlow(id: Long): Flow<TiviShow>

    @Transaction
    @Query("SELECT * FROM shows WHERE id = :id")
    abstract override suspend fun getShowWithIdDetailed(id: Long): ShowDetailed?

    @Transaction
    @Query("SELECT * FROM shows WHERE id = :id")
    abstract override fun getShowDetailedWithIdFlow(id: Long): Flow<ShowDetailed>

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract override suspend fun getShowWithId(id: Long): TiviShow?

    @Query("SELECT trakt_id FROM shows WHERE id = :id")
    abstract override suspend fun getTraktIdForShowId(id: Long): Int?

    @Query("SELECT tmdb_id FROM shows WHERE id = :id")
    abstract override suspend fun getTmdbIdForShowId(id: Long): Int?

    @Query("SELECT id FROM shows WHERE trakt_id = :traktId")
    abstract override suspend fun getIdForTraktId(traktId: Int): Long?

    @Query("SELECT id FROM shows WHERE tmdb_id = :tmdbId")
    abstract override suspend fun getIdForTmdbId(tmdbId: Int): Long?

    @Query("DELETE FROM shows WHERE id = :id")
    abstract override suspend fun delete(id: Long)

    @Query("DELETE FROM shows")
    abstract override suspend fun deleteAll()
}
