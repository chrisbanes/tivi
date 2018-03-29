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
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import io.reactivex.Flowable
import io.reactivex.Maybe
import me.banes.chris.tivi.data.entities.TiviShow
import timber.log.Timber

@Dao
abstract class TiviShowDao {
    @Query("SELECT * FROM shows WHERE trakt_id = :id")
    abstract fun getShowWithTraktIdFlowable(id: Int): Flowable<TiviShow>

    @Query("SELECT * FROM shows WHERE trakt_id = :id")
    abstract fun getShowWithTraktIdMaybe(id: Int): Maybe<TiviShow>

    @Query("SELECT * FROM shows WHERE trakt_id = :id")
    abstract fun getShowWithTraktIdSync(id: Int): TiviShow?

    @Query("SELECT * FROM shows WHERE trakt_id IN (:ids)")
    abstract fun getShowsWithTraktId(ids: List<Int>): Flowable<List<TiviShow>>

    @Query("SELECT * FROM shows WHERE tmdb_id = :id")
    abstract fun getShowWithTmdbIdFlowable(id: Int): Flowable<TiviShow>

    @Query("SELECT * FROM shows WHERE tmdb_id = :id")
    abstract fun getShowWithTmdbIdMaybe(id: Int): Maybe<TiviShow>

    @Query("SELECT * FROM shows WHERE tmdb_id = :id")
    abstract fun getShowWithTmdbIdSync(id: Int): TiviShow?

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract fun getShowWithIdFlowable(id: Long): Flowable<TiviShow>

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract fun getShowWithIdMaybe(id: Long): Maybe<TiviShow>

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract fun getShowWithIdSync(id: Long): TiviShow?

    @Insert
    protected abstract fun insertShow(show: TiviShow): Long

    @Update
    protected abstract fun updateShow(show: TiviShow)

    fun insertOrUpdateShow(show: TiviShow): TiviShow = when {
        show.id == null -> {
            Timber.d("Inserting show: %s", show)
            show.copy(id = insertShow(show))
        }
        else -> {
            Timber.d("Updating show: %s", show)
            updateShow(show)
            show
        }
    }

    @Query("SELECT * FROM shows WHERE tmdb_updated IS null")
    abstract fun getShowsWhichNeedTmdbUpdate(): Flowable<List<TiviShow>>
}
