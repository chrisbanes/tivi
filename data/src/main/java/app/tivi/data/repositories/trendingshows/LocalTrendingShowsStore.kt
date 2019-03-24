/*
 * Copyright 2018 Google LLC
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

package app.tivi.data.repositories.trendingshows

import androidx.paging.DataSource
import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.TrendingDao
import app.tivi.data.entities.TrendingShowEntry
import app.tivi.data.resultentities.TrendingEntryWithShow
import io.reactivex.Flowable
import javax.inject.Inject

class LocalTrendingShowsStore @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val trendingShowsDao: TrendingDao
) {
    fun observeForFlowable(count: Int, offset: Int): Flowable<List<TrendingEntryWithShow>> {
        return trendingShowsDao.entriesFlowable(count, offset)
    }

    fun observeForPaging(): DataSource.Factory<Int, TrendingEntryWithShow> = trendingShowsDao.entriesDataSource()

    suspend fun saveTrendingShowsPage(page: Int, entries: List<TrendingShowEntry>) = transactionRunner {
        trendingShowsDao.deletePage(page)
        trendingShowsDao.insertAll(entries)
    }

    suspend fun deleteAll() = trendingShowsDao.deleteAll()

    suspend fun getLastPage(): Int? = trendingShowsDao.getLastPage()
}