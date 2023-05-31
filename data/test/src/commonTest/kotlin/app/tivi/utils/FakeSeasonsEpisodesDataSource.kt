// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.utils

import app.tivi.data.episodes.SeasonsEpisodesDataSource
import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.Season
import kotlinx.datetime.Instant

class FakeSeasonsEpisodesDataSource : SeasonsEpisodesDataSource {
    var getSeasonsEpisodesResult: Result<List<Pair<Season, List<Episode>>>> = Result.success(emptyList())
    var getShowEpisodeWatchesResult: Result<List<Pair<Episode, EpisodeWatchEntry>>> = Result.success(emptyList())
    var getEpisodeWatchesResult: Result<List<EpisodeWatchEntry>> = Result.success(emptyList())
    var getSeasonWatchesResult: Result<List<Pair<Episode, EpisodeWatchEntry>>> = Result.success(emptyList())
    var addEpisodeWatchesResult: Result<Unit> = Result.success(Unit)
    var removeEpisodeWatchesResult: Result<Unit> = Result.success(Unit)

    override suspend fun getSeasonsEpisodes(showId: Long): List<Pair<Season, List<Episode>>> {
        return getSeasonsEpisodesResult.getOrThrow()
    }

    override suspend fun getShowEpisodeWatches(
        showId: Long,
        since: Instant?,
    ): List<Pair<Episode, EpisodeWatchEntry>> {
        return getShowEpisodeWatchesResult.getOrThrow()
    }

    override suspend fun getEpisodeWatches(
        episodeId: Long,
        since: Instant?,
    ): List<EpisodeWatchEntry> {
        return getEpisodeWatchesResult.getOrThrow()
    }

    override suspend fun getSeasonWatches(
        seasonId: Long,
        since: Instant?,
    ): List<Pair<Episode, EpisodeWatchEntry>> {
        return getSeasonWatchesResult.getOrThrow()
    }

    override suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>) {
        addEpisodeWatchesResult.getOrThrow()
    }

    override suspend fun removeEpisodeWatches(watches: List<EpisodeWatchEntry>) {
        removeEpisodeWatchesResult.getOrThrow()
    }
}
