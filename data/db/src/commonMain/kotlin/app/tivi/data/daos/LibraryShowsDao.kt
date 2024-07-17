// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import androidx.paging.PagingSource
import app.tivi.data.compoundmodels.LibraryShow
import app.tivi.data.models.SortOption

interface LibraryShowsDao {
  fun pagedListLastWatched(
    sort: SortOption,
    filter: String?,
    onlyFollowed: Boolean,
  ): PagingSource<Int, LibraryShow>
}
