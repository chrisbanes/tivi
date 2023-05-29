// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes

import app.tivi.data.models.Episode

interface EpisodeDataSource {
    suspend fun getEpisode(showId: Long, seasonNumber: Int, episodeNumber: Int): Episode
}
