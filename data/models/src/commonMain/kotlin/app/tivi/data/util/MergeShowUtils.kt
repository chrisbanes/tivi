// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.util

import app.tivi.data.models.TiviShow

fun mergeShows(
    local: TiviShow = TiviShow.EMPTY_SHOW,
    trakt: TiviShow = TiviShow.EMPTY_SHOW,
    tmdb: TiviShow = TiviShow.EMPTY_SHOW,
) = local.copy(
    title = trakt.title ?: local.title,
    summary = trakt.summary ?: local.summary,
    homepage = trakt.homepage ?: local.homepage,
    certification = trakt.certification ?: local.certification,
    runtime = trakt.runtime ?: local.runtime,
    country = trakt.country ?: local.country,
    firstAired = trakt.firstAired ?: local.firstAired,
    _genres = trakt._genres ?: local._genres,
    status = trakt.status ?: local.status,
    airsDay = trakt.airsDay ?: local.airsDay,
    airsTimeZone = trakt.airsTimeZone ?: local.airsTimeZone,
    airsTime = trakt.airsTime ?: local.airsTime,

    // Trakt specific stuff
    traktId = trakt.traktId ?: local.traktId,
    traktRating = trakt.traktRating ?: local.traktRating,
    traktVotes = trakt.traktVotes ?: local.traktVotes,

    // TMDb specific stuff
    tmdbId = tmdb.tmdbId ?: trakt.tmdbId ?: local.tmdbId,
    network = tmdb.network ?: trakt.network ?: local.network,
    networkLogoPath = tmdb.networkLogoPath ?: local.networkLogoPath,
)
