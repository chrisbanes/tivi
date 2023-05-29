// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.compoundmodels

import app.tivi.data.models.Episode
import app.tivi.data.models.Season

data class SeasonWithEpisodes(
    val season: Season,
    val episodes: List<Episode> = emptyList(),
)
