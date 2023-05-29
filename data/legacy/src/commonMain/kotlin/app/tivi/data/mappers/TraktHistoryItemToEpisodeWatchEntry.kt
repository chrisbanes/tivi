// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktHistoryItem
import app.tivi.data.models.EpisodeWatchEntry
import me.tatarka.inject.annotations.Inject

@Inject
class TraktHistoryItemToEpisodeWatchEntry : Mapper<TraktHistoryItem, EpisodeWatchEntry> {
    override fun map(from: TraktHistoryItem) = EpisodeWatchEntry(
        episodeId = 0,
        traktId = from.id?.toLong(),
        watchedAt = requireNotNull(from.watchedAt),
    )
}
