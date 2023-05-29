// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.trendingshows

import app.tivi.data.models.TiviShow
import app.tivi.data.models.TrendingShowEntry

fun interface TrendingShowsDataSource {
    suspend operator fun invoke(
        page: Int,
        pageSize: Int,
    ): List<Pair<TiviShow, TrendingShowEntry>>
}
