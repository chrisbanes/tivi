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

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.FollowedShowsEntryWithShow
import app.tivi.data.entities.PendingAction
import io.reactivex.Flowable

@Dao
abstract class FollowedShowsDao : EntryDao<FollowedShowEntry, FollowedShowsEntryWithShow> {
    companion object {
        const val ENTRY_QUERY_ORDER_LAST_WATCHED = "SELECT fs.* FROM myshows_entries as fs" +
                " INNER JOIN seasons AS s ON fs.show_id = s.show_id" +
                " INNER JOIN episodes AS eps ON eps.season_id = s.id" +
                " INNER JOIN episode_watch_entries as ew ON ew.episode_id = eps.id" +
                " GROUP BY fs.id" +
                " ORDER BY datetime(ew.watched_at) DESC"
    }

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED)
    abstract fun entriesBlocking(): List<FollowedShowEntry>

    @Transaction
    @Query("$ENTRY_QUERY_ORDER_LAST_WATCHED LIMIT :count OFFSET :offset")
    abstract override fun entriesFlowable(count: Int, offset: Int): Flowable<List<FollowedShowsEntryWithShow>>

    @Transaction
    @Query(ENTRY_QUERY_ORDER_LAST_WATCHED)
    abstract override fun entriesDataSource(): DataSource.Factory<Int, FollowedShowsEntryWithShow>

    @Query("DELETE FROM myshows_entries")
    abstract override fun deleteAll()

    @Query("SELECT * FROM myshows_entries WHERE id = :id")
    abstract fun entryWithId(id: Long): FollowedShowsEntryWithShow?

    @Query("SELECT * FROM myshows_entries WHERE show_id = :showId")
    abstract fun entryWithShowId(showId: Long): FollowedShowEntry

    @Query("DELETE FROM myshows_entries WHERE show_id = :showId")
    abstract fun deleteWithShowId(showId: Long)

    @Query("SELECT COUNT(*) FROM myshows_entries WHERE show_id = :showId")
    abstract fun entryCountWithShowIdFlowable(showId: Long): Flowable<Int>

    @Query("SELECT COUNT(*) FROM myshows_entries WHERE show_id = :showId")
    abstract fun entryCountWithShowId(showId: Long): Int

    fun entriesWithNoPendingAction() = entriesWithPendingAction(PendingAction.NOTHING.value)

    fun entriesWithSendPendingActions() = entriesWithPendingAction(PendingAction.UPLOAD.value)

    fun entriesWithDeletePendingActions() = entriesWithPendingAction(PendingAction.DELETE.value)

    @Query("SELECT * FROM myshows_entries WHERE pending_action = :pendingAction")
    internal abstract fun entriesWithPendingAction(pendingAction: String): List<FollowedShowEntry>

    @Query("UPDATE myshows_entries SET pending_action = :pendingAction WHERE id IN (:ids)")
    abstract fun updateEntriesToPendingAction(ids: List<Long>, pendingAction: String): Int

    @Query("DELETE FROM myshows_entries WHERE id IN (:ids)")
    abstract fun deleteWithIds(ids: List<Long>): Int
}