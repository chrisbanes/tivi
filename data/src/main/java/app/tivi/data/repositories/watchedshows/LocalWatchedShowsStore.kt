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

import androidx.paging.DataSource
import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.entities.WatchedShowEntry
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.data.syncers.syncerForEntity
import javax.inject.Inject

class LocalWatchedShowsStore @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val watchedShowDao: WatchedShowDao,
    private val showDao: TiviShowDao
) {
    private val syncer = syncerForEntity(
            watchedShowDao,
            { showDao.getTraktIdForShowId(it.showId)!! },
            { entity, id -> entity.copy(id = id ?: 0) }
    )

    suspend fun getWatchedShows(): List<WatchedShowEntryWithShow> = watchedShowDao.entriesWithShow()

    fun observePagedList(): DataSource.Factory<Int, WatchedShowEntryWithShow> = watchedShowDao.entriesDataSource()

    suspend fun sync(watchedShows: List<WatchedShowEntry>) = transactionRunner {
        syncer.sync(watchedShowDao.entries(), watchedShows)
    }
}