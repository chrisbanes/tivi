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
import app.tivi.data.entities.TiviShow
import io.reactivex.Flowable

@Dao
abstract class TiviShowDao : EntityDao<TiviShow> {
    @Query("SELECT * FROM shows WHERE trakt_id = :id")
    abstract suspend fun getShowWithTraktId(id: Int): TiviShow?

    @Query("SELECT * FROM shows WHERE id IN (:ids)")
    abstract fun getShowsWithIds(ids: List<Long>): Flowable<List<TiviShow>>

    @Query("SELECT * FROM shows WHERE tmdb_id = :id")
    abstract suspend fun getShowWithTmdbId(id: Int): TiviShow?

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract fun getShowWithIdFlowable(id: Long): Flowable<TiviShow>

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract suspend fun getShowWithId(id: Long): TiviShow?

    @Query("SELECT trakt_id FROM shows WHERE id = :id")
    abstract suspend fun getTraktIdForShowId(id: Long): Int?

    @Query("SELECT tmdb_id FROM shows WHERE id = :id")
    abstract suspend fun getTmdbIdForShowId(id: Long): Int?

    @Query("SELECT id FROM shows WHERE trakt_id = :traktId")
    abstract suspend fun getIdForTraktId(traktId: Int): Long?
}
