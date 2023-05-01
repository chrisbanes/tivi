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
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.PendingAction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RoomFollowedShowsDao : FollowedShowsDao, RoomEntityDao<FollowedShowEntry> {
    @Query("SELECT * FROM myshows_entries")
    abstract override suspend fun entries(): List<FollowedShowEntry>

    @Query("DELETE FROM myshows_entries")
    abstract override suspend fun deleteAll()

    @Query("SELECT * FROM myshows_entries WHERE show_id = :showId")
    abstract override suspend fun entryWithShowId(showId: Long): FollowedShowEntry?

    @Query("SELECT COUNT(*) FROM myshows_entries WHERE show_id = :showId AND pending_action != 'delete'")
    abstract override fun entryCountWithShowIdNotPendingDeleteObservable(showId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM myshows_entries WHERE show_id = :showId")
    abstract override suspend fun entryCountWithShowId(showId: Long): Int

    override suspend fun entriesWithNoPendingAction(): List<FollowedShowEntry> {
        return entriesWithPendingAction(PendingAction.NOTHING)
    }

    override suspend fun entriesWithSendPendingActions(): List<FollowedShowEntry> {
        return entriesWithPendingAction(PendingAction.UPLOAD)
    }

    override suspend fun entriesWithDeletePendingActions(): List<FollowedShowEntry> {
        return entriesWithPendingAction(PendingAction.DELETE)
    }

    @Query("SELECT * FROM myshows_entries WHERE pending_action = :pendingAction")
    abstract suspend fun entriesWithPendingAction(pendingAction: PendingAction): List<FollowedShowEntry>

    @Query("UPDATE myshows_entries SET pending_action = :pendingAction WHERE id IN (:ids)")
    abstract override suspend fun updateEntriesToPendingAction(ids: List<Long>, pendingAction: PendingAction)

    @Query("DELETE FROM myshows_entries WHERE id IN (:ids)")
    abstract override suspend fun deleteWithIds(ids: List<Long>)
}
