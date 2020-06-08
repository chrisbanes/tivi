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

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.WatchedShowEntry
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WatchedShowDao : EntryDao<WatchedShowEntry, WatchedShowEntryWithShow>() {
    @Transaction
    @Query("SELECT * FROM watched_entries WHERE show_id = :showId")
    abstract suspend fun entryWithShowId(showId: Long): WatchedShowEntry?

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED)
    abstract suspend fun entries(): List<WatchedShowEntry>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED)
    abstract fun entriesObservable(): Flow<List<WatchedShowEntry>>

    fun observePagedList(
        filter: String?,
        sort: SortOption
    ): DataSource.Factory<Int, WatchedShowEntryWithShow> {
        val filtered = filter != null && filter.isNotEmpty()
        return when (sort) {
            SortOption.LAST_WATCHED -> {
                if (filtered) {
                    pagedListLastWatchedFilter("*$filter*")
                } else {
                    pagedListLastWatched()
                }
            }
            SortOption.ALPHABETICAL -> {
                if (filtered) {
                    pagedListAlphaFilter("*$filter*")
                } else {
                    pagedListAlpha()
                }
            }
            else -> throw IllegalArgumentException("$sort option is not supported")
        }
    }

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED)
    protected abstract fun pagedListLastWatched(): DataSource.Factory<Int, WatchedShowEntryWithShow>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED_FILTER)
    protected abstract fun pagedListLastWatchedFilter(filter: String): DataSource.Factory<Int, WatchedShowEntryWithShow>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_ALPHA)
    protected abstract fun pagedListAlpha(): DataSource.Factory<Int, WatchedShowEntryWithShow>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_ALPHA_FILTER)
    protected abstract fun pagedListAlphaFilter(filter: String): DataSource.Factory<Int, WatchedShowEntryWithShow>

    @Query("DELETE FROM watched_entries")
    abstract override suspend fun deleteAll()

    companion object {
        private const val ENTRY_QUERY_ORDER_LAST_WATCHED =
            """
            SELECT we.* FROM watched_entries as we
            ORDER BY datetime(last_watched) DESC
        """

        private const val ENTRY_QUERY_ORDER_LAST_WATCHED_FILTER =
            """
            SELECT we.* FROM watched_entries as we
            INNER JOIN shows_fts AS fts ON we.show_id = fts.docid
            WHERE fts.title MATCH :filter
            ORDER BY datetime(last_watched) DESC
        """

        private const val ENTRY_QUERY_ORDER_ALPHA =
            """
            SELECT we.* FROM watched_entries as we
            INNER JOIN shows_fts AS fts ON we.show_id = fts.docid
            ORDER BY title ASC
        """

        private const val ENTRY_QUERY_ORDER_ALPHA_FILTER =
            """
            SELECT we.* FROM watched_entries as we
            INNER JOIN shows_fts AS fts ON we.show_id = fts.docid
            WHERE title MATCH :filter
            ORDER BY title ASC
        """
    }
}
