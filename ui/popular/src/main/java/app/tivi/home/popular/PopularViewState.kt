// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.popular

import androidx.paging.compose.LazyPagingItems
import app.tivi.data.compoundmodels.PopularEntryWithShow

data class PopularViewState(
    val items: LazyPagingItems<PopularEntryWithShow>,
)
