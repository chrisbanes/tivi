// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.views

data class ShowsWatchStats(
    val showId: Long,
    val episodeCount: Int,
    val watchedEpisodeCount: Int,
)

/**
 * Only exists to make it easier to map from SqlDelight
 */
fun ShowsWatchStats(
    showId: Long,
    episodeCount: Long,
    watchedEpisodeCount: Long,
): ShowsWatchStats = ShowsWatchStats(showId, episodeCount.toInt(), watchedEpisodeCount.toInt())
