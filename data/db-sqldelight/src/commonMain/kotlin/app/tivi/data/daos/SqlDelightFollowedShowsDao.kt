/*
 * Copyright 2023 Google LLC
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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.tivi.data.Database
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.PendingAction
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightFollowedShowsDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : FollowedShowsDao, SqlDelightEntityDao<FollowedShowEntry> {

    override fun upsert(entity: FollowedShowEntry): Long {
        return db.myshows_entriesQueries.upsert(
            entity = entity,
            insert = {
                insert(
                    id = it.id,
                    show_id = it.showId,
                    followed_at = it.followedAt,
                    pending_action = it.pendingAction,
                    trakt_id = it.traktId,
                )
            },
            update = {
                update(
                    id = it.id,
                    show_id = it.showId,
                    followed_at = it.followedAt,
                    pending_action = it.pendingAction,
                    trakt_id = it.traktId,
                )
            },
            lastInsertRowId = { db.myshows_entriesQueries.lastInsertRowId().executeAsOne() },
        )
    }

    override fun entries(): List<FollowedShowEntry> {
        return db.myshows_entriesQueries.entries(::FollowedShowEntry).executeAsList()
    }

    override fun deleteAll() {
        db.myshows_entriesQueries.deleteAll()
    }

    override fun entryWithShowId(showId: Long): FollowedShowEntry? {
        return db.myshows_entriesQueries.entryWithShowId(showId, ::FollowedShowEntry)
            .executeAsOneOrNull()
    }

    override fun entryCountWithShowIdNotPendingDeleteObservable(showId: Long): Flow<Int> {
        return db.myshows_entriesQueries.countOfShowIdNotPendingDeletion(showId)
            .asFlow()
            .mapToOne(dispatchers.io)
            .map { it.toInt() }
    }

    override fun entryCountWithShowId(showId: Long): Int {
        return db.myshows_entriesQueries.countOfShowId(showId).executeAsOne().toInt()
    }

    override fun entriesWithNoPendingAction(): List<FollowedShowEntry> {
        return entriesWithPendingAction(PendingAction.NOTHING)
    }

    override fun entriesWithSendPendingActions(): List<FollowedShowEntry> {
        return entriesWithPendingAction(PendingAction.UPLOAD)
    }

    override fun entriesWithDeletePendingActions(): List<FollowedShowEntry> {
        return entriesWithPendingAction(PendingAction.DELETE)
    }

    private fun entriesWithPendingAction(
        pendingAction: PendingAction,
    ): List<FollowedShowEntry> {
        return db.myshows_entriesQueries.entriesWithPendingAction(
            pendingAction = pendingAction,
            mapper = ::FollowedShowEntry,
        ).executeAsList()
    }

    override fun updateEntriesToPendingAction(
        ids: List<Long>,
        pendingAction: PendingAction,
    ) {
        db.myshows_entriesQueries.updatePendingActionsForIds(pendingAction, ids)
    }

    override fun deleteWithIds(ids: List<Long>) {
        db.myshows_entriesQueries.deleteWithIds(ids)
    }

    override fun deleteEntity(entity: FollowedShowEntry) {
        db.myshows_entriesQueries.deleteWithId(entity.id)
    }
}
