// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.watchedshows

import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry

fun interface WatchedShowsDataSource {
    suspend operator fun invoke(): List<Pair<TiviShow, WatchedShowEntry>>
}
