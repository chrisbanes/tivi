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
import app.tivi.data.util.usingDispatchers
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.withContext
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
  dispatchers: AppCoroutineDispatchers,
) : Store<Int, List<TrendingShowEntry>> by storeBuilder(
  fetcher = Fetcher.of { page: Int ->
    dataSource(page, 20).let { response ->
      withContext(dispatchers.databaseWrite) {
        transactionRunner {
          if (page == 0) {
            lastRequestStore.updateLastRequest()
          }
          response.map { (show, entry) ->
            entry.copy(showId = showDao.getIdOrSavePlaceholder(show), page = page)
          }
        }
      }
    }
  },
  sourceOfTruth = SourceOfTruth.of<Int, List<TrendingShowEntry>, List<TrendingShowEntry>>(
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
    deleteAll = { transactionRunner(trendingShowsDao::deleteAll) },
  ).usingDispatchers(
    readDispatcher = dispatchers.databaseRead,
    writeDispatcher = dispatchers.databaseWrite,
  ),
).validator(
  Validator.by { result ->
    withContext(dispatchers.io) {
      lastRequestStore.isRequestValid(
        threshold = if (result.isNotEmpty()) 3.hours else 30.minutes,
      )
    }
  },
).build()
