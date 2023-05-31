// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.utils

import app.tivi.data.episodes.TraktEpisodeDataSource
import app.tivi.data.models.Episode

class FakeEpisodeDataSource : TraktEpisodeDataSource {
    var result = Result.success(Episode.EMPTY)

    override suspend fun getEpisode(showId: Long, seasonNumber: Int, episodeNumber: Int): Episode {
        return result.getOrThrow()
    }
}
