// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.anticipatedshows

import app.tivi.data.daos.AnticipatedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.daos.updatePage
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.AnticipatedShowEntry
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
class AnticipatedShowsStore(
  dataSource: AnticipatedShowsDataSource,
  anticipatedShowsDao: AnticipatedShowsDao,
  showDao: TiviShowDao,
  lastRequestStore: AnticipatedShowsLastRequestStore,
  transactionRunner: DatabaseTransactionRunner,
  dispatchers: AppCoroutineDispatchers,
) : Store<Int, List<AnticipatedShowEntry>> by storeBuilder(
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
  sourceOfTruth = SourceOfTruth.of<Int, List<AnticipatedShowEntry>, List<AnticipatedShowEntry>>(
    reader = { page -> anticipatedShowsDao.entriesObservable(page) },
    writer = { page, response ->
      transactionRunner {
        if (page == 0) {
          // If we've requested page 0, remove any existing entries first
          anticipatedShowsDao.deleteAll()
          anticipatedShowsDao.upsert(response)
        } else {
          anticipatedShowsDao.updatePage(page, response)
        }
      }
    },
    delete = anticipatedShowsDao::deletePage,
    deleteAll = { transactionRunner(anticipatedShowsDao::deleteAll) },
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
