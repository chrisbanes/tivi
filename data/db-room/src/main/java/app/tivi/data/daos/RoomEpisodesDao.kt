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
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import app.tivi.data.compoundmodels.EpisodeWithSeason
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RoomEpisodesDao : EpisodesDao(), RoomEntityDao<Episode> {
    @Query("SELECT * from episodes WHERE season_id = :seasonId ORDER BY number")
    abstract override suspend fun episodesWithSeasonId(seasonId: Long): List<Episode>

    @Query("DELETE FROM episodes WHERE season_id = :seasonId")
    abstract override suspend fun deleteWithSeasonId(seasonId: Long)

    @Query("SELECT * from episodes WHERE trakt_id = :traktId")
    abstract override suspend fun episodeWithTraktId(traktId: Int): Episode?

    @Query("SELECT * from episodes WHERE tmdb_id = :tmdbId")
    abstract override suspend fun episodeWithTmdbId(tmdbId: Int): Episode?

    @Query("SELECT * from episodes WHERE id = :id")
    abstract override suspend fun episodeWithId(id: Long): Episode?

    @Query("SELECT trakt_id from episodes WHERE id = :id")
    abstract override suspend fun episodeTraktIdForId(id: Long): Int?

    @Query("SELECT id from episodes WHERE trakt_id = :traktId")
    abstract override suspend fun episodeIdWithTraktId(traktId: Int): Long?

    @Transaction
    @Query("SELECT * from episodes WHERE id = :id")
    abstract override fun episodeWithIdObservable(id: Long): Flow<EpisodeWithSeason>

    @Query(
        "SELECT shows.id FROM shows" +
            " INNER JOIN seasons AS s ON s.show_id = shows.id" +
            " INNER JOIN episodes AS eps ON eps.season_id = s.id" +
            " WHERE eps.id = :episodeId",
    )
    abstract override suspend fun showIdForEpisodeId(episodeId: Long): Long

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(latestWatchedEpisodeForShowId)
    abstract override fun observeLatestWatchedEpisodeForShowId(showId: Long): Flow<EpisodeWithSeason?>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(nextEpisodeForShowIdAfter)
    abstract override fun observeNextEpisodeForShowAfter(
        showId: Long,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Flow<EpisodeWithSeason?>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(nextAiredEpisodeForShowIdAfter)
    abstract override fun observeNextAiredEpisodeForShowAfter(
        showId: Long,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Flow<EpisodeWithSeason?>

    companion object {
        const val latestWatchedEpisodeForShowId =
            """
            SELECT eps.*, (100 * s.number) + eps.number AS computed_abs_number
            FROM shows
            INNER JOIN seasons AS s ON shows.id = s.show_id
            INNER JOIN episodes AS eps ON eps.season_id = s.id
            INNER JOIN episode_watch_entries AS ew ON ew.episode_id = eps.id
            WHERE s.number != ${Season.NUMBER_SPECIALS}
                AND s.ignored = 0
                AND shows.id = :showId
            ORDER BY computed_abs_number DESC
            LIMIT 1
            """

        const val nextEpisodeForShowIdAfter =
            """
            SELECT eps.*, (1000 * s.number) + eps.number AS computed_abs_number
            FROM shows
            INNER JOIN seasons AS s ON shows.id = s.show_id
            INNER JOIN episodes AS eps ON eps.season_id = s.id
            WHERE s.number != ${Season.NUMBER_SPECIALS}
                AND s.ignored = 0
                AND shows.id = :showId
                AND computed_abs_number > ((1000 * :seasonNumber) + :episodeNumber)
            ORDER BY computed_abs_number ASC
            LIMIT 1
        """

        const val nextAiredEpisodeForShowIdAfter =
            """
            SELECT eps.*, (1000 * s.number) + eps.number AS computed_abs_number
            FROM shows
            INNER JOIN seasons AS s ON shows.id = s.show_id
            INNER JOIN episodes AS eps ON eps.season_id = s.id
            WHERE s.number != ${Season.NUMBER_SPECIALS}
                AND s.ignored = 0
                AND shows.id = :showId
                AND computed_abs_number > ((1000 * :seasonNumber) + :episodeNumber)
                AND eps.first_aired IS NOT NULL
                AND datetime(eps.first_aired) < datetime('now')
            ORDER BY computed_abs_number ASC
            LIMIT 1
        """
    }
}
