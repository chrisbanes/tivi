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

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import app.tivi.data.compoundmodels.FollowedShowEntryWithShow
import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.PendingAction
import app.tivi.data.models.Season
import app.tivi.data.views.FollowedShowsWatchStats
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FollowedShowsDao : EntryDao<FollowedShowEntry, FollowedShowEntryWithShow>() {
    @Query("SELECT * FROM myshows_entries")
    abstract suspend fun entries(): List<FollowedShowEntry>

    @Transaction
    @Query(
        """
        SELECT myshows_entries.* FROM myshows_entries
            INNER JOIN seasons AS s ON s.show_id = myshows_entries.show_id
			INNER JOIN followed_next_to_watch AS next ON next.show_id = myshows_entries.show_id
			INNER JOIN episodes AS eps ON eps.season_id = s.id
            INNER JOIN episode_watch_entries AS ew ON ew.episode_id = eps.id
            WHERE s.number != ${Season.NUMBER_SPECIALS} AND s.ignored = 0
			ORDER BY datetime(ew.watched_at) DESC
			LIMIT 1
        """,
    )
    abstract fun observeNextShowToWatch(): Flow<FollowedShowEntryWithShow?>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT followed_next_to_watch.* FROM followed_next_to_watch
        INNER JOIN myshows_entries ON myshows_entries.show_id = followed_next_to_watch.show_id
        LEFT JOIN followed_last_watched ON followed_last_watched.id = myshows_entries.id
        LEFT JOIN episode_watch_entries ON episode_watch_entries.episode_id = followed_last_watched.episode_id
        GROUP BY followed_next_to_watch.show_id
        ORDER BY datetime(episode_watch_entries.watched_at) DESC
        """,
    )
    abstract fun pagedUpNextShowsLastWatched(): PagingSource<Int, UpNextEntry>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT followed_next_to_watch.* FROM followed_next_to_watch
        INNER JOIN episodes ON episodes.id = followed_next_to_watch.episode_id
        GROUP BY followed_next_to_watch.show_id
        ORDER BY datetime(episodes.first_aired) DESC
        """,
    )
    abstract fun pagedUpNextShowsDateAired(): PagingSource<Int, UpNextEntry>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT followed_next_to_watch.* FROM followed_next_to_watch
        INNER JOIN myshows_entries ON myshows_entries.show_id = followed_next_to_watch.show_id
        GROUP BY followed_next_to_watch.show_id
        ORDER BY datetime(myshows_entries.followed_at) DESC
        """,
    )
    abstract fun pagedUpNextShowsDateAdded(): PagingSource<Int, UpNextEntry>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM followed_next_to_watch")
    abstract suspend fun getUpNextShows(): List<UpNextEntry>

    @Query("DELETE FROM myshows_entries")
    abstract override suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM myshows_entries WHERE id = :id")
    abstract suspend fun entryWithId(id: Long): FollowedShowEntryWithShow?

    @Query("SELECT * FROM myshows_entries WHERE show_id = :showId")
    abstract suspend fun entryWithShowId(showId: Long): FollowedShowEntry?

    @Query("SELECT COUNT(*) FROM myshows_entries WHERE show_id = :showId AND pending_action != 'delete'")
    abstract fun entryCountWithShowIdNotPendingDeleteObservable(showId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM myshows_entries WHERE show_id = :showId")
    abstract suspend fun entryCountWithShowId(showId: Long): Int

    @Transaction
    @Query(
        """
        SELECT stats.* FROM myshows_view_watch_stats as stats
        INNER JOIN myshows_entries ON stats.id = myshows_entries.id
        WHERE stats.show_id = :showId
        """,
    )
    abstract fun entryShowViewStats(showId: Long): Flow<FollowedShowsWatchStats>

    suspend fun entriesWithNoPendingAction() = entriesWithPendingAction(PendingAction.NOTHING)

    suspend fun entriesWithSendPendingActions() = entriesWithPendingAction(PendingAction.UPLOAD)

    suspend fun entriesWithDeletePendingActions() = entriesWithPendingAction(PendingAction.DELETE)

    @Query("SELECT * FROM myshows_entries WHERE pending_action = :pendingAction")
    internal abstract suspend fun entriesWithPendingAction(pendingAction: PendingAction): List<FollowedShowEntry>

    @Query("UPDATE myshows_entries SET pending_action = :pendingAction WHERE id IN (:ids)")
    abstract suspend fun updateEntriesToPendingAction(ids: List<Long>, pendingAction: PendingAction): Int

    @Query("DELETE FROM myshows_entries WHERE id IN (:ids)")
    abstract suspend fun deleteWithIds(ids: List<Long>): Int

    companion object {
        private const val ENTRY_QUERY_ORDER_LAST_WATCHED = """
            SELECT fs.* FROM myshows_entries as fs
            LEFT JOIN seasons AS s ON fs.show_id = s.show_id
            LEFT JOIN episodes AS eps ON eps.season_id = s.id
            LEFT JOIN episode_watch_entries as ew ON ew.episode_id = eps.id
            GROUP BY fs.id
            ORDER BY MAX(datetime(ew.watched_at)) DESC
        """

        private const val ENTRY_QUERY_ORDER_ALPHA = """
            SELECT fs.* FROM myshows_entries as fs
            INNER JOIN shows_fts AS s_fts ON fs.show_id = s_fts.docid
            ORDER BY title ASC
        """

        private const val ENTRY_QUERY_ORDER_ADDED = """
            SELECT * FROM myshows_entries
            ORDER BY datetime(followed_at) DESC
        """
    }
}
