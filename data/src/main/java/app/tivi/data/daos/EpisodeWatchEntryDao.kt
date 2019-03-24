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
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import io.reactivex.Flowable

@Dao
abstract class EpisodeWatchEntryDao : EntityDao<EpisodeWatchEntry> {
    @Query("SELECT * FROM episode_watch_entries WHERE episode_id = :episodeId")
    abstract suspend fun watchesForEpisode(episodeId: Long): List<EpisodeWatchEntry>

    @Query("SELECT COUNT(id) FROM episode_watch_entries WHERE episode_id = :episodeId")
    abstract suspend fun watchCountForEpisode(episodeId: Long): Int

    @Query("SELECT * FROM episode_watch_entries WHERE episode_id = :episodeId")
    abstract fun watchesForEpisodeFlowable(episodeId: Long): Flowable<List<EpisodeWatchEntry>>

    @Query("SELECT * FROM episode_watch_entries WHERE id = :id")
    abstract suspend fun entryWithId(id: Long): EpisodeWatchEntry?

    @Query("SELECT * FROM episode_watch_entries WHERE trakt_id = :traktId")
    abstract suspend fun entryWithTraktId(traktId: Long): EpisodeWatchEntry?

    suspend fun entriesForShowIdWithNoPendingAction(showId: Long): List<EpisodeWatchEntry> {
        return entriesForShowIdWithPendingAction(showId, PendingAction.NOTHING.value)
    }

    suspend fun entriesForShowIdWithSendPendingActions(showId: Long): List<EpisodeWatchEntry> {
        return entriesForShowIdWithPendingAction(showId, PendingAction.UPLOAD.value)
    }

    suspend fun entriesForShowIdWithDeletePendingActions(showId: Long): List<EpisodeWatchEntry> {
        return entriesForShowIdWithPendingAction(showId, PendingAction.DELETE.value)
    }

    @Query("SELECT ew.* FROM episode_watch_entries AS ew" +
            " INNER JOIN episodes AS eps ON ew.episode_id = eps.id" +
            " INNER JOIN seasons AS s ON eps.season_id = s.id" +
            " INNER JOIN shows ON s.show_id = shows.id" +
            " WHERE shows.id = :showId AND ew.pending_action = :pendingAction")
    internal abstract suspend fun entriesForShowIdWithPendingAction(showId: Long, pendingAction: String): List<EpisodeWatchEntry>

    @Query("SELECT ew.* FROM episode_watch_entries AS ew" +
            " INNER JOIN episodes AS eps ON ew.episode_id = eps.id" +
            " INNER JOIN seasons AS s ON eps.season_id = s.id" +
            " INNER JOIN shows ON s.show_id = shows.id" +
            " WHERE shows.id = :showId")
    abstract suspend fun entriesForShowId(showId: Long): List<EpisodeWatchEntry>

    @Query("UPDATE episode_watch_entries SET pending_action = :pendingAction WHERE id IN (:ids)")
    abstract suspend fun updateEntriesToPendingAction(ids: List<Long>, pendingAction: String): Int

    @Query("DELETE FROM episode_watch_entries WHERE id = :id")
    abstract suspend fun deleteWithId(id: Long): Int

    @Query("DELETE FROM episode_watch_entries WHERE id IN (:ids)")
    abstract suspend fun deleteWithIds(ids: List<Long>): Int

    @Query("DELETE FROM episode_watch_entries WHERE trakt_id = :traktId")
    abstract suspend fun deleteWithTraktId(traktId: Long): Int
}