/*
 * Copyright 2023 Google LLC
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

package app.tivi.data.popularshows

import app.tivi.data.daos.PopularDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.daos.updatePage
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.PopularShowEntry
import app.tivi.inject.ApplicationScope
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder

@ApplicationScope
@Inject
class PopularShowsStore(
    dataSource: PopularShowsDataSource,
    popularShowsDao: PopularDao,
    showDao: TiviShowDao,
    lastRequestStore: PopularShowsLastRequestStore,
    transactionRunner: DatabaseTransactionRunner,
) : Store<Int, List<PopularShowEntry>> by StoreBuilder.from(
    fetcher = Fetcher.of { page: Int ->
        dataSource(page, 20)
            .also {
                if (page == 0) lastRequestStore.updateLastRequest()
            }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { page ->
            popularShowsDao.entriesObservable(page).map { entries ->
                when {
                    // Store only treats null as 'no value', so convert to null
                    entries.isEmpty() -> null
                    // If the request is expired, our data is stale
                    lastRequestStore.isRequestExpired(3.hours) -> null
                    // Otherwise, our data is fresh and valid
                    else -> entries
                }
            }
        },
        writer = { page, response ->
            transactionRunner {
                val entries = response.map { (show, entry) ->
                    entry.copy(showId = showDao.getIdOrSavePlaceholder(show), page = page)
                }
                if (page == 0) {
                    // If we've requested page 0, remove any existing entries first
                    popularShowsDao.deleteAll()
                    popularShowsDao.upsertAll(entries)
                } else {
                    popularShowsDao.updatePage(page, entries)
                }
            }
        },
        delete = popularShowsDao::deletePage,
        deleteAll = popularShowsDao::deleteAll,
    ),
).build()
