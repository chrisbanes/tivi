// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.recommendedshows

import app.tivi.data.daos.RecommendedDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.daos.updatePage
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.RecommendedShowEntry
import app.tivi.data.util.storeBuilder
import app.tivi.data.util.usingDispatchers
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.Validator

@ApplicationScope
@Inject
class RecommendedShowsStore(
  dataSource: RecommendedShowsDataSource,
  recommendedDao: RecommendedDao,
  showDao: TiviShowDao,
  lastRequestStore: RecommendedShowsLastRequestStore,
  transactionRunner: DatabaseTransactionRunner,
  dispatchers: AppCoroutineDispatchers,
) : Store<Int, List<RecommendedShowEntry>> by storeBuilder(
  fetcher = Fetcher.of { page: Int ->
    dataSource(page, 20).let { response ->
      transactionRunner {
        if (page == 0) {
          lastRequestStore.updateLastRequest()
        }
        response.map { show ->
          RecommendedShowEntry(
            showId = showDao.getIdOrSavePlaceholder(show),
            page = page,
          )
        }
      }
    }
  },
  sourceOfTruth = SourceOfTruth.of<Int, List<RecommendedShowEntry>, List<RecommendedShowEntry>>(
    reader = { page -> recommendedDao.entriesForPage(page) },
    writer = { page, response ->
      transactionRunner {
        if (page == 0) {
          // If we've requested page 0, remove any existing entries first
          recommendedDao.deleteAll()
          recommendedDao.upsert(response)
        } else {
          recommendedDao.updatePage(page, response)
        }
      }
    },
    delete = recommendedDao::deletePage,
    deleteAll = { transactionRunner(recommendedDao::deleteAll) },
  ).usingDispatchers(
    readDispatcher = dispatchers.databaseRead,
    writeDispatcher = dispatchers.databaseWrite,
  ),
).validator(
  Validator.by { result ->
    withContext(dispatchers.io) {
      lastRequestStore.isRequestValid(
        threshold = if (result.isNotEmpty()) 3.days else 30.minutes,
      )
    }
  },
).build()
