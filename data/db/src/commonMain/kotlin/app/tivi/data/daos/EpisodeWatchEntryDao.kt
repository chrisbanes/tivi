// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
