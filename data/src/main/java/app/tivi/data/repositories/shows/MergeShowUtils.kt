/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.data.repositories.shows

import app.tivi.data.entities.TiviShow

fun mergeShows(
    local: TiviShow = TiviShow.EMPTY_SHOW,
    trakt: TiviShow = TiviShow.EMPTY_SHOW,
    tmdb: TiviShow = TiviShow.EMPTY_SHOW
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
    traktDataUpdate = trakt.traktDataUpdate ?: local.traktDataUpdate,

    // TMDb specific stuff
    tmdbId = tmdb.tmdbId ?: trakt.tmdbId ?: local.tmdbId,
    network = tmdb.network ?: trakt.network ?: local.network,
    networkLogoPath = tmdb.networkLogoPath ?: local.networkLogoPath
)
