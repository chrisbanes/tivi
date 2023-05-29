// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes

import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.Season
import kotlinx.datetime.Instant

interface SeasonsEpisodesDataSource {
    suspend fun getSeasonsEpisodes(showId: Long): List<Pair<Season, List<Episode>>>
    suspend fun getShowEpisodeWatches(
        showId: Long,
        since: Instant? = null,
    ): List<Pair<Episode, EpisodeWatchEntry>>

    suspend fun getEpisodeWatches(
        episodeId: Long,
        since: Instant? = null,
    ): List<EpisodeWatchEntry>

    suspend fun getSeasonWatches(
        seasonId: Long,
        since: Instant? = null,
    ): List<Pair<Episode, EpisodeWatchEntry>>

    suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>)
    suspend fun removeEpisodeWatches(watches: List<EpisodeWatchEntry>)
}
