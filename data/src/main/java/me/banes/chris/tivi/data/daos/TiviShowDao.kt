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
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Maybe
import me.banes.chris.tivi.data.entities.TiviShow

@Dao
abstract class TiviShowDao : EntityDao<TiviShow> {
    @Query("SELECT * FROM shows WHERE trakt_id = :id")
    abstract fun getShowWithTraktId(id: Int): TiviShow?

    @Query("SELECT * FROM shows WHERE id IN (:ids)")
    abstract fun getShowsWithIds(ids: List<Long>): Flowable<List<TiviShow>>

    @Query("SELECT * FROM shows WHERE tmdb_id = :id")
    abstract fun getShowWithTmdbId(id: Int): TiviShow?

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract fun getShowWithIdFlowable(id: Long): Flowable<TiviShow>

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract fun getShowWithIdMaybe(id: Long): Maybe<TiviShow>

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract fun getShowWithId(id: Long): TiviShow?
}
