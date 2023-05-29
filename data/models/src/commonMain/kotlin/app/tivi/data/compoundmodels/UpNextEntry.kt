// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.compoundmodels

import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import app.tivi.data.models.TiviShow

data class UpNextEntry(
    val show: TiviShow,
    val season: Season,
    val episode: Episode,
)
