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
import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.models.Season
import app.tivi.data.models.Season.Companion.NUMBER_SPECIALS
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RoomSeasonsDao : SeasonsDao, RoomEntityDao<Season> {
    @Transaction
    @Query("SELECT * FROM seasons WHERE show_id = :showId ORDER BY number=$NUMBER_SPECIALS, number")
    abstract override fun seasonsWithEpisodesForShowId(showId: Long): Flow<List<SeasonWithEpisodesAndWatches>>

    @Query("SELECT * FROM seasons WHERE show_id = :showId ORDER BY number=$NUMBER_SPECIALS, number")
    abstract override fun observeSeasonsForShowId(showId: Long): Flow<List<Season>>

    override fun observeSeasonWithId(id: Long): Flow<Season> {
        TODO("Not yet implemented")
    }

    @Query("SELECT * FROM seasons WHERE show_id = :showId ORDER BY number=$NUMBER_SPECIALS, number")
    abstract override suspend fun seasonsForShowId(showId: Long): List<Season>

    @Transaction
    @Query("SELECT * FROM seasons WHERE id = :seasonId")
    abstract override fun seasonWithEpisodes(seasonId: Long): Flow<SeasonWithEpisodesAndWatches>

    @Query("DELETE FROM seasons WHERE show_id = :showId")
    abstract override suspend fun deleteWithShowId(showId: Long)

    @Query("SELECT * FROM seasons WHERE id = :id")
    abstract override suspend fun seasonWithId(id: Long): Season?

    @Query("SELECT trakt_id FROM seasons WHERE id = :id")
    abstract override suspend fun traktIdForId(id: Long): Int?

    @Query("SELECT * FROM seasons WHERE trakt_id = :traktId")
    abstract override suspend fun seasonWithTraktId(traktId: Int): Season?

    @Query(
        """
        SELECT id from seasons WHERE
          show_id = (SELECT show_id from SEASONS WHERE id = :seasonId)
          AND number != $NUMBER_SPECIALS
          AND number < (SELECT number from SEASONS WHERE id = :seasonId)
    """,
    )
    abstract override suspend fun showPreviousSeasonIds(seasonId: Long): LongArray

    @Query("UPDATE seasons SET ignored = :ignored WHERE id = :seasonId")
    abstract override suspend fun updateSeasonIgnoreFlag(seasonId: Long, ignored: Boolean)

    @Query("SELECT * FROM seasons WHERE show_id = :showId AND number = :number")
    abstract override suspend fun seasonWithShowIdAndNumber(showId: Long, number: Int): Season?
}
