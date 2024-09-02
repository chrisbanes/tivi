// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import androidx.paging.PagingSource
import app.tivi.data.compoundmodels.AnticipatedShowEntryWithShow
import app.tivi.data.models.AnticipatedShowEntry
import kotlinx.coroutines.flow.Flow

interface AnticipatedShowsDao : PaginatedEntryDao<AnticipatedShowEntry, AnticipatedShowEntryWithShow> {

  fun entriesObservable(page: Int): Flow<List<AnticipatedShowEntry>>

  fun entriesObservable(count: Int, offset: Int): Flow<List<AnticipatedShowEntryWithShow>>

  fun entriesPagingSource(): PagingSource<Int, AnticipatedShowEntryWithShow>

  override fun deletePage(page: Int)

  override fun deleteAll()

  override fun getLastPage(): Int?
}
