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

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.tivi.data.entities.RecommendedShowEntry
import app.tivi.data.resultentities.RecommendedEntryWithShow
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RecommendedDao : PaginatedEntryDao<RecommendedShowEntry, RecommendedEntryWithShow>() {
    @Transaction
    @Query("SELECT * FROM recommended_entries WHERE page = :page ORDER BY id ASC")
    abstract fun entriesForPage(page: Int): Flow<List<RecommendedShowEntry>>

    @Transaction
    @Query("SELECT * FROM recommended_entries ORDER BY page ASC, id ASC LIMIT :count OFFSET :offset")
    abstract fun entriesObservable(count: Int, offset: Int): Flow<List<RecommendedEntryWithShow>>

    @Transaction
    @Query("SELECT * FROM recommended_entries ORDER BY page ASC, id ASC")
    abstract fun entriesDataSource(): DataSource.Factory<Int, RecommendedEntryWithShow>

    @Query("DELETE FROM recommended_entries WHERE page = :page")
    abstract override suspend fun deletePage(page: Int)

    @Query("DELETE FROM recommended_entries")
    abstract override suspend fun deleteAll()

    @Query("SELECT MAX(page) from recommended_entries")
    abstract override suspend fun getLastPage(): Int?
}
