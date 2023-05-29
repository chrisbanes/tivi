// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.compoundmodels

import app.tivi.data.models.Episode
import app.tivi.data.models.Season

data class EpisodeWithSeason(
    val episode: Episode,
    val season: Season,
)
