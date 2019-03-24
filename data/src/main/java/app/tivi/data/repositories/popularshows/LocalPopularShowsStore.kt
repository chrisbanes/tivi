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

package app.tivi.data.repositories.popularshows

import androidx.paging.DataSource
import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.PopularDao
import app.tivi.data.entities.PopularShowEntry
import app.tivi.data.resultentities.PopularEntryWithShow
import io.reactivex.Flowable
import javax.inject.Inject

class LocalPopularShowsStore @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val popularShowDao: PopularDao
) {
    fun observeForFlowable(count: Int, offset: Int): Flowable<List<PopularEntryWithShow>> {
        return popularShowDao.entriesFlowable(count, offset)
    }

    fun observeForPaging(): DataSource.Factory<Int, PopularEntryWithShow> {
        return popularShowDao.entriesDataSource()
    }

    suspend fun savePopularShowsPage(page: Int, entries: List<PopularShowEntry>) = transactionRunner {
        popularShowDao.deletePage(page)
        popularShowDao.insertAll(entries)
    }

    suspend fun deleteAll() = popularShowDao.deleteAll()

    suspend fun getLastPage(): Int? = popularShowDao.getLastPage()
}