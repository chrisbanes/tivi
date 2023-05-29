// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.trending

import androidx.paging.compose.LazyPagingItems
import app.tivi.data.compoundmodels.TrendingEntryWithShow

data class TrendingShowsViewState(
    val items: LazyPagingItems<TrendingEntryWithShow>,
)
