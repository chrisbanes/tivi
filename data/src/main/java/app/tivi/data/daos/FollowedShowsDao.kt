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
import androidx.room.Transaction
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.data.views.FollowedShowsWatchStats
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FollowedShowsDao : EntryDao<FollowedShowEntry, FollowedShowEntryWithShow>() {
    @Query("SELECT * FROM myshows_entries")
    abstract suspend fun entries(): List<FollowedShowEntry>

    @Transaction
    @Query(ENTRY_QUERY_SUPER_SORT)
    internal abstract fun pagedListSuperSort(): PagingSource<Int, FollowedShowEntryWithShow>

    @Transaction
    @Query(ENTRY_QUERY_SUPER_SORT_FILTER)
    internal abstract fun pagedListSuperSortFilter(filter: String): PagingSource<Int, FollowedShowEntryWithShow>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED)
    internal abstract fun pagedListLastWatched(): PagingSource<Int, FollowedShowEntryWithShow>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED_FILTER)
    internal abstract fun pagedListLastWatchedFilter(filter: String): PagingSource<Int, FollowedShowEntryWithShow>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_ALPHA)
    internal abstract fun pagedListAlpha(): PagingSource<Int, FollowedShowEntryWithShow>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_ALPHA_FILTER)
    internal abstract fun pagedListAlphaFilter(filter: String): PagingSource<Int, FollowedShowEntryWithShow>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_ADDED)
    internal abstract fun pagedListAdded(): PagingSource<Int, FollowedShowEntryWithShow>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_ADDED_FILTER)
    internal abstract fun pagedListAddedFilter(filter: String): PagingSource<Int, FollowedShowEntryWithShow>

    @Transaction
    @Query(
        """
        SELECT myshows_entries.* FROM myshows_entries
            INNER JOIN seasons AS s ON s.show_id = myshows_entries.show_id
			INNER JOIN followed_next_to_watch AS next ON next.id = myshows_entries.id
			INNER JOIN episodes AS eps ON eps.season_id = s.id
            INNER JOIN episode_watch_entries AS ew ON ew.episode_id = eps.id
            WHERE s.number != ${Season.NUMBER_SPECIALS} AND s.ignored = 0
			ORDER BY datetime(ew.watched_at) DESC
			LIMIT 1
    """
    )
    abstract fun observeNextShowToWatch(): Flow<FollowedShowEntryWithShow?>

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
        SELECT stats.* FROM FollowedShowsWatchStats as stats
        INNER JOIN myshows_entries ON stats.id = myshows_entries.id
        WHERE show_id = :showId
    """
    )
    abstract fun entryShowViewStats(showId: Long): Flow<FollowedShowsWatchStats>

    suspend fun entriesWithNoPendingAction() = entriesWithPendingAction(PendingAction.NOTHING.value)

    suspend fun entriesWithSendPendingActions() = entriesWithPendingAction(PendingAction.UPLOAD.value)

    suspend fun entriesWithDeletePendingActions() = entriesWithPendingAction(PendingAction.DELETE.value)

    @Query("SELECT * FROM myshows_entries WHERE pending_action = :pendingAction")
    internal abstract suspend fun entriesWithPendingAction(pendingAction: String): List<FollowedShowEntry>

    @Query("UPDATE myshows_entries SET pending_action = :pendingAction WHERE id IN (:ids)")
    abstract suspend fun updateEntriesToPendingAction(ids: List<Long>, pendingAction: String): Int

    @Query("DELETE FROM myshows_entries WHERE id IN (:ids)")
    abstract suspend fun deleteWithIds(ids: List<Long>): Int

    companion object {
        private const val ENTRY_QUERY_SUPER_SORT = """
            SELECT fs.* FROM myshows_entries as fs
            LEFT JOIN seasons AS s ON fs.show_id = s.show_id
            LEFT JOIN episodes AS eps ON eps.season_id = s.id
            LEFT JOIN episode_watch_entries as ew ON ew.episode_id = eps.id
            LEFT JOIN followed_next_to_watch as nw ON nw.id = fs.id
            WHERE s.number != ${Season.NUMBER_SPECIALS}
                AND s.ignored = 0
            GROUP BY fs.id
            ORDER BY
                /* shows with aired episodes to watch first */
                SUM(CASE WHEN datetime(first_aired) < datetime('now') THEN 1 ELSE 0 END) = COUNT(watched_at) ASC,
                /* latest event */
                MAX(
                    MAX(datetime(coalesce(next_ep_to_watch_air_date, 0))), /* next episode to watch */
                    MAX(datetime(coalesce(watched_at, 0))), /* last watch */
                    MAX(datetime(coalesce(followed_at, 0))) /* when followed */
                ) DESC
        """

        private const val ENTRY_QUERY_SUPER_SORT_FILTER = """
            SELECT fs.* FROM myshows_entries as fs
            INNER JOIN shows_fts AS s_fts ON fs.show_id = s_fts.docid
            LEFT JOIN seasons AS s ON fs.show_id = s.show_id
            LEFT JOIN episodes AS eps ON eps.season_id = s.id
            LEFT JOIN episode_watch_entries as ew ON ew.episode_id = eps.id
            LEFT JOIN followed_next_to_watch as nw ON nw.id = fs.id
            WHERE s.number != ${Season.NUMBER_SPECIALS}
                AND s.ignored = 0
                AND s_fts.title MATCH :filter
            GROUP BY fs.id
            ORDER BY
                /* shows with aired episodes to watch first */
                SUM(CASE WHEN datetime(first_aired) < datetime('now') THEN 1 ELSE 0 END) = COUNT(watched_at) ASC,
                /* latest event */
                MAX(
                    MAX(datetime(coalesce(next_ep_to_watch_air_date, 0))), /* next episode to watch */
                    MAX(datetime(coalesce(watched_at, 0))), /* last watch */
                    MAX(datetime(coalesce(followed_at, 0))) /* when followed */
                ) DESC
        """

        private const val ENTRY_QUERY_ORDER_LAST_WATCHED = """
            SELECT fs.* FROM myshows_entries as fs
            LEFT JOIN seasons AS s ON fs.show_id = s.show_id
            LEFT JOIN episodes AS eps ON eps.season_id = s.id
            LEFT JOIN episode_watch_entries as ew ON ew.episode_id = eps.id
            GROUP BY fs.id
            ORDER BY MAX(datetime(ew.watched_at)) DESC
        """

        private const val ENTRY_QUERY_ORDER_LAST_WATCHED_FILTER = """
            SELECT fs.* FROM myshows_entries as fs
            INNER JOIN shows_fts AS s_fts ON fs.show_id = s_fts.docid
            LEFT JOIN seasons AS s ON fs.show_id = s.show_id
            LEFT JOIN episodes AS eps ON eps.season_id = s.id
            LEFT JOIN episode_watch_entries as ew ON ew.episode_id = eps.id
            WHERE s_fts.title MATCH :filter
            GROUP BY fs.id
            ORDER BY MAX(datetime(ew.watched_at)) DESC
        """

        private const val ENTRY_QUERY_ORDER_ALPHA = """
            SELECT fs.* FROM myshows_entries as fs
            INNER JOIN shows_fts AS s_fts ON fs.show_id = s_fts.docid
            ORDER BY title ASC
        """

        private const val ENTRY_QUERY_ORDER_ALPHA_FILTER = """
            SELECT fs.* FROM myshows_entries as fs
            INNER JOIN shows_fts AS s_fts ON fs.show_id = s_fts.docid
            WHERE s_fts.title MATCH :filter
            ORDER BY title ASC
        """

        private const val ENTRY_QUERY_ORDER_ADDED = """
            SELECT * FROM myshows_entries
            ORDER BY datetime(followed_at) DESC
        """

        private const val ENTRY_QUERY_ORDER_ADDED_FILTER = """
            SELECT fs.* FROM myshows_entries as fs
            INNER JOIN shows_fts AS s_fts ON fs.show_id = s_fts.docid
            WHERE s_fts.title MATCH :filter
            ORDER BY datetime(followed_at) DESC
        """
    }
}
