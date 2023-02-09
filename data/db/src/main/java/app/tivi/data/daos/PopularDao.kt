/*
 * Copyright 2017 Google LLC
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
import app.tivi.data.compoundmodels.PopularEntryWithShow
import app.tivi.data.models.PopularShowEntry
import kotlinx.coroutines.flow.Flow

interface PopularDao : PaginatedEntryDao<PopularShowEntry, PopularEntryWithShow> {

    fun entriesObservable(page: Int): Flow<List<PopularShowEntry>>

    fun entriesObservable(count: Int, offset: Int): Flow<List<PopularEntryWithShow>>

    fun entriesPagingSource(): PagingSource<Int, PopularEntryWithShow>

    override suspend fun deletePage(page: Int)

    override suspend fun deleteAll()

    override suspend fun getLastPage(): Int?
}
