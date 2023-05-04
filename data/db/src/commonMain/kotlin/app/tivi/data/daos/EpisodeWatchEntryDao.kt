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

import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.PendingAction
import kotlinx.coroutines.flow.Flow

interface EpisodeWatchEntryDao : EntityDao<EpisodeWatchEntry> {

    fun watchesForEpisode(episodeId: Long): List<EpisodeWatchEntry>

    fun watchCountForEpisode(episodeId: Long): Int

    fun watchesForEpisodeObservable(episodeId: Long): Flow<List<EpisodeWatchEntry>>

    fun entryWithId(id: Long): EpisodeWatchEntry?

    fun entryWithTraktId(traktId: Long): EpisodeWatchEntry?

    fun entryIdWithTraktId(traktId: Long): Long?

    fun entriesForShowIdWithNoPendingAction(showId: Long): List<EpisodeWatchEntry>

    fun entriesForShowIdWithSendPendingActions(showId: Long): List<EpisodeWatchEntry>

    fun entriesForShowIdWithDeletePendingActions(showId: Long): List<EpisodeWatchEntry>

    fun entriesForShowId(showId: Long): List<EpisodeWatchEntry>

    fun updateEntriesToPendingAction(ids: List<Long>, pendingAction: PendingAction)

    fun deleteWithId(id: Long)

    fun deleteWithIds(ids: List<Long>)

    fun deleteWithTraktId(traktId: Long)
}
