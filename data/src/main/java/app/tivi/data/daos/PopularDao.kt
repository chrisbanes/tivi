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

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.tivi.data.entities.PopularShowEntry
import app.tivi.data.resultentities.PopularEntryWithShow
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PopularDao : PaginatedEntryDao<PopularShowEntry, PopularEntryWithShow>() {
    @Transaction
    @Query("SELECT * FROM popular_shows WHERE page = :page ORDER BY page_order")
    abstract fun entriesObservable(page: Int): Flow<List<PopularShowEntry>>

    @Transaction
    @Query("SELECT * FROM popular_shows ORDER BY page, page_order LIMIT :count OFFSET :offset")
    abstract fun entriesObservable(count: Int, offset: Int): Flow<List<PopularEntryWithShow>>

    @Transaction
    @Query("SELECT * FROM popular_shows ORDER BY page, page_order")
    abstract fun entriesDataSource(): DataSource.Factory<Int, PopularEntryWithShow>

    @Query("DELETE FROM popular_shows WHERE page = :page")
    abstract override suspend fun deletePage(page: Int)

    @Query("DELETE FROM popular_shows")
    abstract override suspend fun deleteAll()

    @Query("SELECT MAX(page) from popular_shows")
    abstract override suspend fun getLastPage(): Int?
}
