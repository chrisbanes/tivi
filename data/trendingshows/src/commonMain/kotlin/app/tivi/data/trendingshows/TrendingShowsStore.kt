// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.trendingshows

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.TrendingDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.daos.updatePage
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.TrendingShowEntry
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
class TrendingShowsStore(
    dataSource: TrendingShowsDataSource,
    trendingShowsDao: TrendingDao,
    showDao: TiviShowDao,
    lastRequestStore: TrendingShowsLastRequestStore,
    transactionRunner: DatabaseTransactionRunner,
) : Store<Int, List<TrendingShowEntry>> by StoreBuilder.from(
    fetcher = Fetcher.of { page: Int ->
        dataSource(page, 20)
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
                    trendingShowsDao.deleteAll()
                    trendingShowsDao.upsert(entries)
                } else {
                    trendingShowsDao.updatePage(page, entries)
                }
            }
        },
        delete = trendingShowsDao::deletePage,
        deleteAll = trendingShowsDao::deleteAll,
    ),
).build()
