// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.trendingshows

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.TrendingDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.daos.updatePage
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.TrendingShowEntry
import app.tivi.data.util.storeBuilder
import app.tivi.inject.ApplicationScope
import kotlin.time.Duration.Companion.hours
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.Validator

@ApplicationScope
@Inject
class TrendingShowsStore(
    dataSource: TrendingShowsDataSource,
    trendingShowsDao: TrendingDao,
    showDao: TiviShowDao,
    lastRequestStore: TrendingShowsLastRequestStore,
    transactionRunner: DatabaseTransactionRunner,
) : Store<Int, List<TrendingShowEntry>> by storeBuilder(
    fetcher = Fetcher.of { page: Int ->
        dataSource(page, 20).let { response ->
            transactionRunner {
                if (page == 0) {
                    lastRequestStore.updateLastRequest()
                }
                response.map { (show, entry) ->
                    entry.copy(showId = showDao.getIdOrSavePlaceholder(show), page = page)
                }
            }
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { page -> trendingShowsDao.entriesObservable(page) },
        writer = { page, response ->
            transactionRunner {
                if (page == 0) {
                    // If we've requested page 0, remove any existing entries first
                    trendingShowsDao.deleteAll()
                    trendingShowsDao.upsert(response)
                } else {
                    trendingShowsDao.updatePage(page, response)
                }
            }
        },
        delete = trendingShowsDao::deletePage,
        deleteAll = trendingShowsDao::deleteAll,
    ),
).validator(
    Validator.by { lastRequestStore.isRequestValid(3.hours) },
).build()
