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
import androidx.room.Transaction
import app.tivi.data.models.WatchedShowEntry
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

    @Query("SELECT is_dirty FROM watched_entries WHERE show_id = :showId")
    abstract override fun isDirty(showId: Long): Boolean?

    @Query("UPDATE watched_entries SET is_dirty = 0 WHERE show_id = :showId")
    abstract override fun resetDirty(showId: Long)

    companion object {
        private const val ENTRY_QUERY_ORDER_LAST_WATCHED = """
            SELECT we.* FROM watched_entries as we
            ORDER BY datetime(last_watched) DESC
        """
    }
}
