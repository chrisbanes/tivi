/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.data.daos

import app.cash.paging.PagingSource
import app.tivi.data.compoundmodels.RecommendedEntryWithShow
import app.tivi.data.models.RecommendedShowEntry
import kotlinx.coroutines.flow.Flow

interface RecommendedDao : PaginatedEntryDao<RecommendedShowEntry, RecommendedEntryWithShow> {

    fun entriesForPage(page: Int): Flow<List<RecommendedShowEntry>>

    fun entriesObservable(count: Int, offset: Int): Flow<List<RecommendedEntryWithShow>>

    fun entriesPagingSource(): PagingSource<Int, RecommendedEntryWithShow>

    override suspend fun deletePage(page: Int)

    override suspend fun deleteAll()

    override suspend fun getLastPage(): Int?
}
