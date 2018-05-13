/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.trakt.calls

import android.arch.paging.DataSource
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Users
import io.reactivex.Flowable
import kotlinx.coroutines.experimental.withContext
import me.banes.chris.tivi.ShowFetcher
import me.banes.chris.tivi.calls.ListCall
import me.banes.chris.tivi.data.DatabaseTransactionRunner
import me.banes.chris.tivi.data.daos.WatchedShowDao
import me.banes.chris.tivi.data.entities.WatchedShowEntry
import me.banes.chris.tivi.data.entities.WatchedShowListItem
import me.banes.chris.tivi.extensions.fetchBodyWithRetry
import me.banes.chris.tivi.extensions.parallelForEach
import me.banes.chris.tivi.extensions.parallelMap
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject
import javax.inject.Provider

class WatchedShowsCall @Inject constructor(
    private val databaseTransactionRunner: DatabaseTransactionRunner,
    private val watchShowDao: WatchedShowDao,
    private val showFetcher: ShowFetcher,
    private val usersService: Provider<Users>,
    private val schedulers: AppRxSchedulers,
    private val dispatchers: AppCoroutineDispatchers
) : ListCall<Unit, WatchedShowListItem> {

    override val pageSize = 21

    fun data() = data(Unit)

    override fun data(param: Unit): Flowable<List<WatchedShowListItem>> {
        return watchShowDao.entries()
                .distinctUntilChanged()
                .subscribeOn(schedulers.database)
    }

    override fun dataSourceFactory(): DataSource.Factory<Int, WatchedShowListItem> = watchShowDao.entriesDataSource()

    override suspend fun refresh(param: Unit) {
        val networkResponse = withContext(dispatchers.network) {
            usersService.get().watchedShows(UserSlug.ME, Extended.NOSEASONS).fetchBodyWithRetry()
        }

        val shows = networkResponse.parallelMap { traktEntry ->
            val showId = showFetcher.insertPlaceholderIfNeeded(traktEntry.show)
            WatchedShowEntry(null, showId, traktEntry.last_watched_at)
        }

        // Now save it to the database
        withContext(dispatchers.database) {
            databaseTransactionRunner.runInTransaction {
                watchShowDao.deleteAll()
                shows.forEach { watchShowDao.insert(it) }
            }
        }

        shows.parallelForEach {
            // Now trigger a refresh of each show
            showFetcher.update(it.showId)
        }
    }
}