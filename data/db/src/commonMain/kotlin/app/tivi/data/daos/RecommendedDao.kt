// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.paging.PagingSource
import app.tivi.data.compoundmodels.RecommendedEntryWithShow
import app.tivi.data.models.RecommendedShowEntry
import kotlinx.coroutines.flow.Flow

interface RecommendedDao : PaginatedEntryDao<RecommendedShowEntry, RecommendedEntryWithShow> {

    fun entriesForPage(page: Int): Flow<List<RecommendedShowEntry>>

    fun entriesObservable(count: Int, offset: Int): Flow<List<RecommendedEntryWithShow>>

    fun entriesPagingSource(): PagingSource<Int, RecommendedEntryWithShow>

    override fun deletePage(page: Int)

    override fun deleteAll()

    override fun getLastPage(): Int?
}
