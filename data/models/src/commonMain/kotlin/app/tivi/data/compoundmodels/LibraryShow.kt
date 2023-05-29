// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.compoundmodels

import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.views.ShowsWatchStats

data class LibraryShow(
    val show: TiviShow,
    val stats: ShowsWatchStats?,
    val watchedEntry: WatchedShowEntry?,
)
