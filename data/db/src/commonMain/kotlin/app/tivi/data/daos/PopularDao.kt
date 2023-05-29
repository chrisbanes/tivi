// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.paging.PagingSource
import app.tivi.data.compoundmodels.PopularEntryWithShow
import app.tivi.data.models.PopularShowEntry
import kotlinx.coroutines.flow.Flow

interface PopularDao : PaginatedEntryDao<PopularShowEntry, PopularEntryWithShow> {

    fun entriesObservable(page: Int): Flow<List<PopularShowEntry>>

    fun entriesObservable(count: Int, offset: Int): Flow<List<PopularEntryWithShow>>

    fun entriesPagingSource(): PagingSource<Int, PopularEntryWithShow>

    override fun deletePage(page: Int)

    override fun deleteAll()

    override fun getLastPage(): Int?
}
