// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.anticipatedshows

import app.tivi.data.models.AnticipatedShowEntry
import app.tivi.data.models.TiviShow

fun interface AnticipatedShowsDataSource {
  suspend operator fun invoke(
    page: Int,
    pageSize: Int,
  ): List<Pair<TiviShow, AnticipatedShowEntry>>
}
