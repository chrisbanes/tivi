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
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.PendingAction
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightEpisodeWatchEntryDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : EpisodeWatchEntryDao, SqlDelightEntityDao<EpisodeWatchEntry> {

    override fun insert(entity: EpisodeWatchEntry): Long {
        db.episode_watch_entriesQueries.insert(
            id = entity.id,
            episode_id = entity.episodeId,
            trakt_id = entity.traktId,
            watched_at = entity.watchedAt,
            pending_action = entity.pendingAction,
        )
        return db.episode_watch_entriesQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: EpisodeWatchEntry) {
        db.episode_watch_entriesQueries.update(
            id = entity.id,
            episode_id = entity.episodeId,
            trakt_id = entity.traktId,
            watched_at = entity.watchedAt,
            pending_action = entity.pendingAction,
        )
    }

    override fun watchesForEpisode(episodeId: Long): List<EpisodeWatchEntry> {
        return db.episode_watch_entriesQueries.watchesForEpisodeId(episodeId, ::EpisodeWatchEntry)
            .executeAsList()
    }

    override fun watchCountForEpisode(episodeId: Long): Int {
        return db.episode_watch_entriesQueries.watchCountForEpisodeId(episodeId)
            .executeAsOne()
            .toInt()
    }

    override fun watchesForEpisodeObservable(episodeId: Long): Flow<List<EpisodeWatchEntry>> {
        return db.episode_watch_entriesQueries.watchesForEpisodeId(episodeId, ::EpisodeWatchEntry)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override fun entryWithId(id: Long): EpisodeWatchEntry? {
        return db.episode_watch_entriesQueries.entryWithId(id, ::EpisodeWatchEntry)
            .executeAsOneOrNull()
    }

    override fun entryWithTraktId(traktId: Long): EpisodeWatchEntry? {
        return db.episode_watch_entriesQueries.entryWithTraktId(traktId, ::EpisodeWatchEntry)
            .executeAsOneOrNull()
    }

    override fun entryIdWithTraktId(traktId: Long): Long? {
        return db.episode_watch_entriesQueries.idForTraktId(traktId).executeAsOneOrNull()
    }

    override fun entriesForShowIdWithNoPendingAction(showId: Long): List<EpisodeWatchEntry> {
        return db.episode_watch_entriesQueries.entriesForShowIdWithPendingAction(
            showId = showId,
            pendingAction = PendingAction.NOTHING,
            mapper = ::EpisodeWatchEntry,
        ).executeAsList()
    }

    override fun entriesForShowIdWithSendPendingActions(showId: Long): List<EpisodeWatchEntry> {
        return db.episode_watch_entriesQueries.entriesForShowIdWithPendingAction(
            showId = showId,
            pendingAction = PendingAction.UPLOAD,
            mapper = ::EpisodeWatchEntry,
        ).executeAsList()
    }

    override fun entriesForShowIdWithDeletePendingActions(showId: Long): List<EpisodeWatchEntry> {
        return db.episode_watch_entriesQueries.entriesForShowIdWithPendingAction(
            showId = showId,
            pendingAction = PendingAction.DELETE,
            mapper = ::EpisodeWatchEntry,
        ).executeAsList()
    }

    override fun entriesForShowId(showId: Long): List<EpisodeWatchEntry> {
        return db.episode_watch_entriesQueries.entriesForShowId(showId, ::EpisodeWatchEntry)
            .executeAsList()
    }

    override fun updateEntriesToPendingAction(
        ids: List<Long>,
        pendingAction: PendingAction,
    ) {
        db.episode_watch_entriesQueries.updatePendingActionForIds(pendingAction, ids)
    }

    override fun deleteWithId(id: Long) {
        db.episode_watch_entriesQueries.deleteWithId(id)
    }

    override fun deleteWithIds(ids: List<Long>) {
        db.episode_watch_entriesQueries.deleteWithIds(ids)
    }

    override fun deleteWithTraktId(traktId: Long) {
        db.episode_watch_entriesQueries.deleteWithTraktId(traktId)
    }

    override fun deleteEntity(entity: EpisodeWatchEntry) {
        deleteWithId(entity.id)
    }
}
