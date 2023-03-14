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
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import app.cash.paging.PagingSource
import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.models.Season
import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.views.ShowsWatchStats
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RoomWatchedShowDao : WatchedShowDao, RoomEntityDao<WatchedShowEntry> {
    @Transaction
    @Query("SELECT * FROM watched_entries WHERE show_id = :showId")
    abstract override suspend fun entryWithShowId(showId: Long): WatchedShowEntry?

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED)
    abstract override suspend fun entries(): List<WatchedShowEntry>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED)
    abstract override fun entriesObservable(): Flow<List<WatchedShowEntry>>

    @Query("DELETE FROM watched_entries")
    abstract override suspend fun deleteAll()

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT shows_next_to_watch.* FROM shows_next_to_watch
        LEFT JOIN shows_last_watched ON shows_last_watched.show_id = shows_next_to_watch.show_id
        LEFT JOIN episode_watch_entries ON episode_watch_entries.episode_id = shows_last_watched.episode_id
        GROUP BY shows_next_to_watch.show_id
        ORDER BY datetime(episode_watch_entries.watched_at) DESC
        """,
    )
    abstract override fun pagedUpNextShowsLastWatched(): PagingSource<Int, UpNextEntry>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT shows_next_to_watch.* FROM shows_next_to_watch
        INNER JOIN episodes ON episodes.id = shows_next_to_watch.episode_id
        GROUP BY shows_next_to_watch.show_id
        ORDER BY datetime(episodes.first_aired) DESC
        """,
    )
    abstract override fun pagedUpNextShowsDateAired(): PagingSource<Int, UpNextEntry>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM shows_next_to_watch")
    abstract override suspend fun getUpNextShows(): List<UpNextEntry>

    @Query("SELECT * FROM shows_view_watch_stats WHERE show_id = :showId")
    abstract override fun entryShowViewStats(showId: Long): Flow<ShowsWatchStats>

    @Transaction
    @Query(
        """
        SELECT shows.* FROM shows
            INNER JOIN seasons AS s ON s.show_id = shows.id
			INNER JOIN shows_next_to_watch AS next ON next.show_id = shows.id
			INNER JOIN episodes AS eps ON eps.season_id = s.id
            INNER JOIN episode_watch_entries AS ew ON ew.episode_id = eps.id
            WHERE s.number != ${Season.NUMBER_SPECIALS} AND s.ignored = 0
			ORDER BY datetime(ew.watched_at) DESC
			LIMIT 1
        """,
    )
    abstract override fun observeNextShowToWatch(): Flow<TiviShow?>

    companion object {
        private const val ENTRY_QUERY_ORDER_LAST_WATCHED = """
            SELECT we.* FROM watched_entries as we
            ORDER BY datetime(last_watched) DESC
        """
    }
}
