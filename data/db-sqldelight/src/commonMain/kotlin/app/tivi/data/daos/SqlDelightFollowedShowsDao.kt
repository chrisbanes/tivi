// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.tivi.data.Database
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.PendingAction
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightFollowedShowsDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : FollowedShowsDao, SqlDelightEntityDao<FollowedShowEntry> {

    override fun insert(entity: FollowedShowEntry): Long {
        db.myshows_entriesQueries.insert(
            id = entity.id,
            show_id = entity.showId,
            followed_at = entity.followedAt,
            pending_action = entity.pendingAction,
            trakt_id = entity.traktId,
        )
        return db.myshows_entriesQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: FollowedShowEntry) {
        db.myshows_entriesQueries.update(
            id = entity.id,
            show_id = entity.showId,
            followed_at = entity.followedAt,
            pending_action = entity.pendingAction,
            trakt_id = entity.traktId,
        )
    }

    override fun entries(): List<FollowedShowEntry> {
        return db.myshows_entriesQueries.entries(::FollowedShowEntry).executeAsList()
    }

    override fun deleteAll() = db.myshows_entriesQueries.transaction {
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
        db.myshows_entriesQueries.transaction {
            db.myshows_entriesQueries.updatePendingActionsForIds(pendingAction, ids)
        }
    }

    override fun deleteWithIds(ids: List<Long>) {
        db.myshows_entriesQueries.transaction {
            db.myshows_entriesQueries.deleteWithIds(ids)
        }
    }

    override fun deleteEntity(entity: FollowedShowEntry) {
        db.myshows_entriesQueries.transaction {
            db.myshows_entriesQueries.deleteWithId(entity.id)
        }
    }
}
