// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.compoundmodels.FollowedShowEntryWithShow
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.PendingAction
import kotlinx.coroutines.flow.Flow

interface FollowedShowsDao : EntryDao<FollowedShowEntry, FollowedShowEntryWithShow> {

    fun entries(): List<FollowedShowEntry>

    override fun deleteAll()

    fun entryWithShowId(showId: Long): FollowedShowEntry?

    fun entryCountWithShowIdNotPendingDeleteObservable(showId: Long): Flow<Int>

    fun entryCountWithShowId(showId: Long): Int

    fun entriesWithNoPendingAction(): List<FollowedShowEntry>

    fun entriesWithSendPendingActions(): List<FollowedShowEntry>

    fun entriesWithDeletePendingActions(): List<FollowedShowEntry>

    fun updateEntriesToPendingAction(ids: List<Long>, pendingAction: PendingAction)

    fun deleteWithIds(ids: List<Long>)
}
