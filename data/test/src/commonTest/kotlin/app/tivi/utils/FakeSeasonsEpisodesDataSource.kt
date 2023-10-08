// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.utils

import app.tivi.data.episodes.datasource.SeasonsEpisodesDataSource
import app.tivi.data.models.Episode
import app.tivi.data.models.Season

class FakeSeasonsEpisodesDataSource : SeasonsEpisodesDataSource {
    var getSeasonsEpisodesResult: Result<List<Pair<Season, List<Episode>>>> = Result.success(emptyList())

    override suspend fun getSeasonsEpisodes(showId: Long): List<Pair<Season, List<Episode>>> {
        return getSeasonsEpisodesResult.getOrThrow()
    }
}
