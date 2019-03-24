/*
 * Copyright 2018 Google LLC
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
import app.tivi.data.entities.Season
import app.tivi.data.entities.Season.Companion.NUMBER_SPECIALS
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import io.reactivex.Flowable

@Dao
abstract class SeasonsDao : EntityDao<Season> {
    @Transaction
    @Query("SELECT * FROM seasons WHERE show_id = :showId ORDER BY number=$NUMBER_SPECIALS, number")
    abstract fun seasonsWithEpisodesForShowId(showId: Long): Flowable<List<SeasonWithEpisodesAndWatches>>

    @Query("SELECT * FROM seasons WHERE show_id = :showId ORDER BY number=$NUMBER_SPECIALS, number")
    abstract suspend fun seasonsForShowId(showId: Long): List<Season>

    @Query("DELETE FROM seasons WHERE show_id = :showId")
    abstract suspend fun deleteWithShowId(showId: Long)

    @Query("DELETE FROM seasons WHERE show_id = :showId")
    abstract suspend fun deleteSeasonsForShowId(showId: Long): Int

    @Query("SELECT * FROM seasons WHERE id = :id")
    abstract suspend fun seasonWithId(id: Long): Season?

    @Query("SELECT trakt_id FROM seasons WHERE id = :id")
    abstract suspend fun traktIdForId(id: Long): Int?

    @Query("SELECT * FROM seasons WHERE trakt_id = :traktId")
    abstract suspend fun seasonWithTraktId(traktId: Int): Season?

    @Query("SELECT * FROM seasons WHERE show_id = :showId AND number = :number")
    abstract suspend fun seasonWithShowIdAndNumber(showId: Long, number: Int): Season?
}