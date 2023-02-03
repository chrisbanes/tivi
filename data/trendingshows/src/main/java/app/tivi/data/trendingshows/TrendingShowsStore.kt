/*
 * Copyright 2020 Google LLC
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

package app.tivi.data.trendingshows

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.TrendingDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.daos.updatePage
import app.tivi.data.models.TrendingShowEntry
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.threeten.bp.Duration

@Singleton
class TrendingShowsStore @Inject constructor(
    traktTrendingShows: TraktTrendingShowsDataSource,
    trendingShowsDao: TrendingDao,
    showDao: TiviShowDao,
    lastRequestStore: TrendingShowsLastRequestStore,
) : Store<Int, List<TrendingShowEntry>> by StoreBuilder.from(
    fetcher = Fetcher.of { page: Int ->
        traktTrendingShows(page, 20)
            .also {
                if (page == 0) {
                    lastRequestStore.updateLastRequest()
                }
            }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { page ->
            trendingShowsDao.entriesObservable(page).map { entries ->
                when {
                    // Store only treats null as 'no value', so convert to null
                    entries.isEmpty() -> null
                    // If the request is expired, our data is stale
                    lastRequestStore.isRequestExpired(Duration.ofHours(3)) -> null
                    // Otherwise, our data is fresh and valid
                    else -> entries
                }
            }
        },
        writer = { page, response ->
            trendingShowsDao.withTransaction {
                val entries = response.map { (show, entry) ->
                    entry.copy(showId = showDao.getIdOrSavePlaceholder(show), page = page)
                }
                if (page == 0) {
                    // If we've requested page 0, remove any existing entries first
                    trendingShowsDao.deleteAll()
                    trendingShowsDao.insertAll(entries)
                } else {
                    trendingShowsDao.updatePage(page, entries)
                }
            }
        },
        delete = trendingShowsDao::deletePage,
        deleteAll = trendingShowsDao::deleteAll,
    ),
).build()