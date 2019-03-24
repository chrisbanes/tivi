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
import app.tivi.data.entities.TrendingShowEntry
import app.tivi.data.resultentities.TrendingEntryWithShow
import io.reactivex.Flowable

@Dao
abstract class TrendingDao : PaginatedEntryDao<TrendingShowEntry, TrendingEntryWithShow> {
    @Transaction
    @Query("SELECT * FROM trending_shows ORDER BY page ASC, watchers DESC, id ASC LIMIT :count OFFSET :offset")
    abstract override fun entriesFlowable(count: Int, offset: Int): Flowable<List<TrendingEntryWithShow>>

    @Transaction
    @Query("SELECT * FROM trending_shows ORDER BY page ASC, watchers DESC, id ASC")
    abstract override fun entriesDataSource(): DataSource.Factory<Int, TrendingEntryWithShow>

    @Query("DELETE FROM trending_shows WHERE page = :page")
    abstract override suspend fun deletePage(page: Int)

    @Query("DELETE FROM trending_shows")
    abstract override suspend fun deleteAll()

    @Query("SELECT MAX(page) from trending_shows")
    abstract override suspend fun getLastPage(): Int?
}