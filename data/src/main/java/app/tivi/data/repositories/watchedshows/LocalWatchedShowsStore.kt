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

package app.tivi.data.repositories.watchedshows

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.entities.WatchedShowEntry
import javax.inject.Inject

class LocalWatchedShowsStore @Inject constructor(
    private val entityInserter: EntityInserter,
    private val transactionRunner: DatabaseTransactionRunner,
    private val watchedShowDao: WatchedShowDao
) {
    suspend fun getWatchedShows() = watchedShowDao.entries()

    fun observePagedList() = watchedShowDao.entriesDataSource()

    suspend fun saveWatchedShows(watchedShows: List<WatchedShowEntry>) = transactionRunner {
        watchedShowDao.deleteAll()
        entityInserter.insertOrUpdate(watchedShowDao, watchedShows)
    }
}