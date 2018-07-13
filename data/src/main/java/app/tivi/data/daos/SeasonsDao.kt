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

package app.tivi.data.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import app.tivi.data.entities.Season
import app.tivi.data.entities.Season.Companion.NUMBER_SPECIALS
import app.tivi.data.resultentities.SeasonWithEpisodes
import io.reactivex.Flowable

@Dao
abstract class SeasonsDao : EntityDao<Season> {
    @Transaction
    @Query("SELECT * FROM seasons WHERE show_id = :showId ORDER BY number=$NUMBER_SPECIALS, number")
    abstract fun seasonsWithEpisodesForShowId(showId: Long): Flowable<List<SeasonWithEpisodes>>

    @Query("SELECT * FROM seasons WHERE show_id = :showId ORDER BY number=$NUMBER_SPECIALS, number")
    abstract fun seasonsForShowId(showId: Long): List<Season>

    @Query("DELETE FROM seasons WHERE show_id = :showId")
    abstract fun deleteSeasonsForShowId(showId: Long): Int

    @Query("SELECT * FROM seasons WHERE id = :id")
    abstract fun seasonWithId(id: Long): Season?

    @Query("SELECT * FROM seasons WHERE trakt_id = :traktId")
    abstract fun seasonWithSeasonTraktId(traktId: Int): Season?
}