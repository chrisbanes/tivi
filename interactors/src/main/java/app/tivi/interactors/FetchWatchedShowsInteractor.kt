/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.interactors

import app.tivi.ShowFetcher
import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.entities.WatchedShowEntry
import app.tivi.extensions.fetchBodyWithRetry
import app.tivi.extensions.parallelForEach
import app.tivi.extensions.parallelMap
import app.tivi.util.AppCoroutineDispatchers
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Users
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject
import javax.inject.Provider

class FetchWatchedShowsInteractor @Inject constructor(
    private val databaseTransactionRunner: DatabaseTransactionRunner,
    private val watchShowDao: WatchedShowDao,
    private val showFetcher: ShowFetcher,
    private val usersService: Provider<Users>,
    private val dispatchers: AppCoroutineDispatchers
) : Interactor<Unit> {
    override val dispatcher: CoroutineDispatcher
        get() = dispatchers.io

    override suspend fun invoke(param: Unit) {
        val networkResponse = withContext(dispatchers.io) {
            usersService.get().watchedShows(UserSlug.ME, Extended.NOSEASONS).fetchBodyWithRetry()
        }

        val shows = networkResponse.parallelMap { traktEntry ->
            val showId = showFetcher.insertPlaceholderIfNeeded(traktEntry.show)
            WatchedShowEntry(null, showId, traktEntry.last_watched_at)
        }

        // Now save it to the database
        databaseTransactionRunner.runInTransaction {
            watchShowDao.deleteAll()
            watchShowDao.insertAll(shows)
        }

        shows.parallelForEach {
            // Now trigger a refresh of each show
            showFetcher.update(it.showId)
        }
    }
}
