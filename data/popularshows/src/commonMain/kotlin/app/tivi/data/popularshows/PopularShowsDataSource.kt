// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.popularshows

import app.tivi.data.models.PopularShowEntry
import app.tivi.data.models.TiviShow

fun interface PopularShowsDataSource {
    suspend operator fun invoke(
        page: Int,
        pageSize: Int,
    ): List<Pair<TiviShow, PopularShowEntry>>
}
