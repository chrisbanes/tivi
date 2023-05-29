// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.compoundmodels

import app.tivi.data.models.Episode
import app.tivi.data.models.Season

data class SeasonWithEpisodesAndWatches(
    val season: Season,
    val episodes: List<EpisodeWithWatches> = emptyList(),
) {
    val numberAiredToWatch: Int by lazy {
        episodes.count { !it.isWatched && it.episode.hasAired }
    }

    val numberWatched: Int by lazy {
        episodes.count { it.isWatched }
    }

    val numberToAir: Int by lazy {
        episodes.size - numberAired
    }

    val numberAired: Int by lazy {
        episodes.count { it.episode.hasAired }
    }

    val nextToAir: Episode? by lazy {
        episodes.firstOrNull {
            it.episode.let { ep -> !ep.hasAired && ep.firstAired != null }
        }?.episode
    }
}
