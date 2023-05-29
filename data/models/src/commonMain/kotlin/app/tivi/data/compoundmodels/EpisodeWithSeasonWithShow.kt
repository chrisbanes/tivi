// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.compoundmodels

import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import app.tivi.data.models.TiviShow

data class EpisodeWithSeasonWithShow(
    val episode: Episode,
    val season: Season,
    val show: TiviShow,
)
