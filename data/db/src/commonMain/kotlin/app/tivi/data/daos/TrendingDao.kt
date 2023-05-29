// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.paging.PagingSource
import app.tivi.data.compoundmodels.TrendingEntryWithShow
import app.tivi.data.models.TrendingShowEntry
import kotlinx.coroutines.flow.Flow

interface TrendingDao : PaginatedEntryDao<TrendingShowEntry, TrendingEntryWithShow> {

    fun entriesObservable(page: Int): Flow<List<TrendingShowEntry>>

    fun entriesObservable(count: Int, offset: Int): Flow<List<TrendingEntryWithShow>>

    fun entriesPagingSource(): PagingSource<Int, TrendingEntryWithShow>

    override fun deletePage(page: Int)

    override fun deleteAll()

    override fun getLastPage(): Int?
}
