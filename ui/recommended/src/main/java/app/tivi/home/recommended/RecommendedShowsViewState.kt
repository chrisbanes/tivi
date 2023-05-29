// Copyright 2022, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.recommended

import androidx.paging.compose.LazyPagingItems
import app.tivi.data.compoundmodels.RecommendedEntryWithShow

data class RecommendedShowsViewState(
    val items: LazyPagingItems<RecommendedEntryWithShow>,
)
