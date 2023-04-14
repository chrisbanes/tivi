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

import app.tivi.data.compoundmodels.FollowedShowEntryWithShow
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.PendingAction
import kotlinx.coroutines.flow.Flow

interface FollowedShowsDao : EntryDao<FollowedShowEntry, FollowedShowEntryWithShow> {

    suspend fun entries(): List<FollowedShowEntry>

    override suspend fun deleteAll()

    suspend fun entryWithShowId(showId: Long): FollowedShowEntry?

    fun entryCountWithShowIdNotPendingDeleteObservable(showId: Long): Flow<Int>

    suspend fun entryCountWithShowId(showId: Long): Int

    suspend fun entriesWithNoPendingAction(): List<FollowedShowEntry>

    suspend fun entriesWithSendPendingActions(): List<FollowedShowEntry>

    suspend fun entriesWithDeletePendingActions(): List<FollowedShowEntry>

    suspend fun entriesWithPendingAction(pendingAction: PendingAction): List<FollowedShowEntry>

    suspend fun updateEntriesToPendingAction(ids: List<Long>, pendingAction: PendingAction): Int

    suspend fun deleteWithIds(ids: List<Long>)
}
