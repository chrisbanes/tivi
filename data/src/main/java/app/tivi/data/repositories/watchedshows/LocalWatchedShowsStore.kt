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
import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.entities.Request
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.WatchedShowEntry
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import org.threeten.bp.Instant
import javax.inject.Inject

class LocalWatchedShowsStore @Inject constructor(
    private val entityInserter: EntityInserter,
    private val transactionRunner: DatabaseTransactionRunner,
    private val watchedShowDao: WatchedShowDao,
    private val lastRequestDao: LastRequestDao
) {
    suspend fun getWatchedShows() = watchedShowDao.entries()

    suspend fun getWatchedShow(showId: Long) = watchedShowDao.entryWithShowId(showId)

    fun observePagedList(filter: String?, sort: SortOption): DataSource.Factory<Int, WatchedShowEntryWithShow> {
        val filtered = filter != null && filter.isNotEmpty()
        return when (sort) {
            SortOption.LAST_WATCHED -> {
                if (filtered) {
                    watchedShowDao.pagedListLastWatchedFilter("*$filter*")
                } else {
                    watchedShowDao.pagedListLastWatched()
                }
            }
            SortOption.ALPHABETICAL -> {
                if (filtered) {
                    watchedShowDao.pagedListAlphaFilter("*$filter*")
                } else {
                    watchedShowDao.pagedListAlpha()
                }
            }
            else -> throw IllegalArgumentException("$sort option is not supported")
        }
    }

    suspend fun saveWatchedShows(watchedShows: List<WatchedShowEntry>) = transactionRunner {
        watchedShowDao.deleteAll()
        entityInserter.insertOrUpdate(watchedShowDao, watchedShows)
    }

    suspend fun updateLastWatchedShowsRequest(instant: Instant) {
        lastRequestDao.updateLastRequest(Request.WATCHED_SHOWS, 0, instant)
    }

    suspend fun getLastWatchedShowsRequest(): Instant? {
        return lastRequestDao.getRequestInstant(Request.WATCHED_SHOWS, 0)
    }
}