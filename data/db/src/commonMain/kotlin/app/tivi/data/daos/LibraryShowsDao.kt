// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.paging.PagingSource
import app.tivi.data.compoundmodels.LibraryShow
import app.tivi.data.models.SortOption

interface LibraryShowsDao {
    fun pagedListLastWatched(
        sort: SortOption,
        filter: String?,
        includeWatched: Boolean,
        includeFollowed: Boolean,
    ): PagingSource<Int, LibraryShow>
}
