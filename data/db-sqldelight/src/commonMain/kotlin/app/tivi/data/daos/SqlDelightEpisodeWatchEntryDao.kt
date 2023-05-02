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
import app.cash.sqldelight.coroutines.mapToList
import app.tivi.data.Database
import app.tivi.data.await
import app.tivi.data.awaitAsNull
import app.tivi.data.awaitList
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.PendingAction
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightEpisodeWatchEntryDao(
    override val db: Database,
    override val dispatchers: AppCoroutineDispatchers,
) : EpisodeWatchEntryDao, SqlDelightEntityDao<EpisodeWatchEntry> {
    override fun upsertBlocking(entity: EpisodeWatchEntry): Long {
        return db.episode_watch_entriesQueries.upsert(
            entity = entity,
            insert = {
                insert(
                    id = it.id,
                    episode_id = it.episodeId,
                    trakt_id = it.traktId,
                    watched_at = it.watchedAt,
                    pending_action = it.pendingAction,
                )
            },
            update = {
                update(
                    id = it.id,
                    episode_id = it.episodeId,
                    trakt_id = it.traktId,
                    watched_at = it.watchedAt,
                    pending_action = it.pendingAction,
                )
            },
            lastInsertRowId = { lastInsertRowId().executeAsOne() },
        )
    }

    override suspend fun watchesForEpisode(episodeId: Long): List<EpisodeWatchEntry> {
        return db.episode_watch_entriesQueries.watchesForEpisodeId(episodeId, ::EpisodeWatchEntry)
            .awaitList(dispatchers.io)
    }

    override suspend fun watchCountForEpisode(episodeId: Long): Int {
        return db.episode_watch_entriesQueries.watchCountForEpisodeId(episodeId)
            .await(dispatchers.io)
            .toInt()
    }

    override fun watchesForEpisodeObservable(episodeId: Long): Flow<List<EpisodeWatchEntry>> {
        return db.episode_watch_entriesQueries.watchesForEpisodeId(episodeId, ::EpisodeWatchEntry)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override suspend fun entryWithId(id: Long): EpisodeWatchEntry? {
        return db.episode_watch_entriesQueries.entryWithId(id, ::EpisodeWatchEntry)
            .awaitAsNull(dispatchers.io)
    }

    override suspend fun entryWithTraktId(traktId: Long): EpisodeWatchEntry? {
        return db.episode_watch_entriesQueries.entryWithTraktId(traktId, ::EpisodeWatchEntry)
            .awaitAsNull(dispatchers.io)
    }

    override suspend fun entryIdWithTraktId(traktId: Long): Long? {
        return db.episode_watch_entriesQueries.idForTraktId(traktId).awaitAsNull(dispatchers.io)
    }

    override suspend fun entriesForShowIdWithNoPendingAction(showId: Long): List<EpisodeWatchEntry> {
        return db.episode_watch_entriesQueries.entriesForShowIdWithPendingAction(
            showId = showId,
            pendingAction = PendingAction.NOTHING,
            mapper = ::EpisodeWatchEntry,
        ).awaitList(dispatchers.io)
    }

    override suspend fun entriesForShowIdWithSendPendingActions(showId: Long): List<EpisodeWatchEntry> {
        return db.episode_watch_entriesQueries.entriesForShowIdWithPendingAction(
            showId = showId,
            pendingAction = PendingAction.UPLOAD,
            mapper = ::EpisodeWatchEntry,
        ).awaitList(dispatchers.io)
    }

    override suspend fun entriesForShowIdWithDeletePendingActions(showId: Long): List<EpisodeWatchEntry> {
        return db.episode_watch_entriesQueries.entriesForShowIdWithPendingAction(
            showId = showId,
            pendingAction = PendingAction.DELETE,
            mapper = ::EpisodeWatchEntry,
        ).awaitList(dispatchers.io)
    }

    override suspend fun entriesForShowId(showId: Long): List<EpisodeWatchEntry> {
        return db.episode_watch_entriesQueries.entriesForShowId(showId, ::EpisodeWatchEntry)
            .awaitList(dispatchers.io)
    }

    override suspend fun updateEntriesToPendingAction(
        ids: List<Long>,
        pendingAction: PendingAction,
    ): Unit = withContext(dispatchers.io) {
        db.episode_watch_entriesQueries.updatePendingActionForIds(pendingAction, ids)
    }

    override suspend fun deleteWithId(id: Long): Unit = withContext(dispatchers.io) {
        db.episode_watch_entriesQueries.deleteWithId(id)
    }

    override suspend fun deleteWithIds(ids: List<Long>): Unit = withContext(dispatchers.io) {
        db.episode_watch_entriesQueries.deleteWithIds(ids)
    }

    override suspend fun deleteWithTraktId(traktId: Long): Unit = withContext(dispatchers.io) {
        db.episode_watch_entriesQueries.deleteWithTraktId(traktId)
    }

    override suspend fun deleteEntity(entity: EpisodeWatchEntry) {
        deleteWithId(entity.id)
    }
}
