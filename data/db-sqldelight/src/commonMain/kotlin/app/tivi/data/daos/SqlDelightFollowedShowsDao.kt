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
import app.tivi.data.await
import app.tivi.data.awaitAsNull
import app.tivi.data.awaitList
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.PendingAction
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightFollowedShowsDao(
    override val db: Database,
    override val dispatchers: AppCoroutineDispatchers,
) : FollowedShowsDao, SqlDelightEntityDao<FollowedShowEntry> {

    override fun upsertBlocking(entity: FollowedShowEntry): Long {
        return db.myshows_entriesQueries.upsert(
            entity = entity,
            insert = { entity ->
                insert(
                    id = entity.id,
                    show_id = entity.showId,
                    followed_at = entity.followedAt,
                    pending_action = entity.pendingAction,
                    trakt_id = entity.traktId,
                )
            },
            update = { entity ->
                update(
                    id = entity.id,
                    show_id = entity.showId,
                    followed_at = entity.followedAt,
                    pending_action = entity.pendingAction,
                    trakt_id = entity.traktId,
                )
            },
            lastInsertRowId = { db.myshows_entriesQueries.lastInsertRowId().executeAsOne() },
        )
    }

    override suspend fun entries(): List<FollowedShowEntry> {
        return db.myshows_entriesQueries.entries(::FollowedShowEntry).awaitList(dispatchers.io)
    }

    override suspend fun deleteAll() = withContext(dispatchers.io) {
        db.myshows_entriesQueries.deleteAll()
    }

    override suspend fun entryWithShowId(showId: Long): FollowedShowEntry? {
        return db.myshows_entriesQueries.entryWithShowId(showId, ::FollowedShowEntry)
            .awaitAsNull(dispatchers.io)
    }

    override fun entryCountWithShowIdNotPendingDeleteObservable(showId: Long): Flow<Int> {
        return db.myshows_entriesQueries.countOfShowIdNotPendingDeletion(showId)
            .asFlow()
            .mapToOne(dispatchers.io)
            .map { it.toInt() }
    }

    override suspend fun entryCountWithShowId(showId: Long): Int {
        return db.myshows_entriesQueries.countOfShowId(showId).await(dispatchers.io).toInt()
    }

    override suspend fun entriesWithNoPendingAction(): List<FollowedShowEntry> {
        return entriesWithPendingAction(PendingAction.NOTHING)
    }

    override suspend fun entriesWithSendPendingActions(): List<FollowedShowEntry> {
        return entriesWithPendingAction(PendingAction.UPLOAD)
    }

    override suspend fun entriesWithDeletePendingActions(): List<FollowedShowEntry> {
        return entriesWithPendingAction(PendingAction.DELETE)
    }

    private suspend fun entriesWithPendingAction(
        pendingAction: PendingAction,
    ): List<FollowedShowEntry> {
        return db.myshows_entriesQueries.entriesWithPendingAction(
            pendingAction = pendingAction,
            mapper = ::FollowedShowEntry,
        ).awaitList(dispatchers.io)
    }

    override suspend fun updateEntriesToPendingAction(
        ids: List<Long>,
        pendingAction: PendingAction,
    ): Unit = withContext(dispatchers.io) {
        db.myshows_entriesQueries.updatePendingActionsForIds(pendingAction, ids)
    }

    override suspend fun deleteWithIds(ids: List<Long>): Unit = withContext(dispatchers.io) {
        db.myshows_entriesQueries.deleteWithIds(ids)
    }

    override suspend fun deleteEntity(entity: FollowedShowEntry): Unit = withContext(dispatchers.io) {
        db.myshows_entriesQueries.deleteWithId(entity.id)
    }
}
