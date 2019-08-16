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

package app.tivi.data.repositories.recommendedshows

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.RecommendedDao
import app.tivi.data.entities.RecommendedShowEntry
import javax.inject.Inject

class RecommendedShowsStore @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val recommendedDao: RecommendedDao
) {
    fun observeForObservable(count: Int, offset: Int) = recommendedDao.entriesObservable(count, offset)

    fun observeForPaging() = recommendedDao.entriesDataSource()

    suspend fun savePage(page: Int, entries: List<RecommendedShowEntry>) = transactionRunner {
        recommendedDao.deletePage(page)
        recommendedDao.insertAll(entries)
    }

    suspend fun deleteAll() = recommendedDao.deleteAll()

    suspend fun getLastPage(): Int? = recommendedDao.getLastPage()
}