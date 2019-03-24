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

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import io.reactivex.Flowable

@Dao
abstract class FollowedShowsDao : EntryDao<FollowedShowEntry, FollowedShowEntryWithShow> {
    companion object {
        const val ENTRY_QUERY_ORDER_LAST_WATCHED = "SELECT fs.*, MAX(datetime(ew.watched_at)) AS watched_at" +
                " FROM myshows_entries as fs" +
                " INNER JOIN seasons AS s ON fs.show_id = s.show_id" +
                " INNER JOIN episodes AS eps ON eps.season_id = s.id" +
                " INNER JOIN episode_watch_entries as ew ON ew.episode_id = eps.id" +
                " GROUP BY fs.id" +
                " ORDER BY watched_at DESC"

        const val ENTRY_QUERY_ORDER_ADDED = "SELECT * FROM myshows_entries ORDER BY datetime(followed_at) DESC"
    }

    @Query("SELECT * FROM myshows_entries")
    abstract suspend fun entries(): List<FollowedShowEntry>

    @Transaction
    @Query("$ENTRY_QUERY_ORDER_LAST_WATCHED LIMIT :count OFFSET :offset")
    abstract override fun entriesFlowable(count: Int, offset: Int): Flowable<List<FollowedShowEntryWithShow>>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED)
    abstract override fun entriesDataSource(): DataSource.Factory<Int, FollowedShowEntryWithShow>

    @Query("DELETE FROM myshows_entries")
    abstract override suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM myshows_entries WHERE id = :id")
    abstract suspend fun entryWithId(id: Long): FollowedShowEntryWithShow?

    @Query("SELECT * FROM myshows_entries WHERE show_id = :showId")
    abstract suspend fun entryWithShowId(showId: Long): FollowedShowEntry?

    @Query("SELECT COUNT(*) FROM myshows_entries WHERE show_id = :showId AND pending_action != 'delete'")
    abstract fun entryCountWithShowIdNotPendingDeleteFlowable(showId: Long): Flowable<Int>

    @Query("SELECT COUNT(*) FROM myshows_entries WHERE show_id = :showId")
    abstract suspend fun entryCountWithShowId(showId: Long): Int

    suspend fun entriesWithNoPendingAction() = entriesWithPendingAction(PendingAction.NOTHING.value)

    suspend fun entriesWithSendPendingActions() = entriesWithPendingAction(PendingAction.UPLOAD.value)

    suspend fun entriesWithDeletePendingActions() = entriesWithPendingAction(PendingAction.DELETE.value)

    @Query("SELECT * FROM myshows_entries WHERE pending_action = :pendingAction")
    internal abstract suspend fun entriesWithPendingAction(pendingAction: String): List<FollowedShowEntry>

    @Query("UPDATE myshows_entries SET pending_action = :pendingAction WHERE id IN (:ids)")
    abstract suspend fun updateEntriesToPendingAction(ids: List<Long>, pendingAction: String): Int

    @Query("DELETE FROM myshows_entries WHERE id IN (:ids)")
    abstract suspend fun deleteWithIds(ids: List<Long>): Int
}