// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.watchedshows

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.util.storeBuilder
import app.tivi.data.util.syncerForEntity
import app.tivi.data.util.usingDispatchers
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
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
class WatchedShowsStore(
  dataSource: WatchedShowsDataSource,
  watchedShowsDao: WatchedShowDao,
  showDao: TiviShowDao,
  lastRequestStore: WatchedShowsLastRequestStore,
  logger: Logger,
  transactionRunner: DatabaseTransactionRunner,
  dispatchers: AppCoroutineDispatchers,
) : Store<Unit, List<WatchedShowEntry>> by storeBuilder(
  fetcher = Fetcher.of {
    dataSource().let { response ->
      transactionRunner {
        lastRequestStore.updateLastRequest()

        response.map { (show, entry) ->
          entry.copy(showId = showDao.getIdOrSavePlaceholder(show))
        }
      }
    }
  },
  sourceOfTruth = SourceOfTruth.of<Unit, List<WatchedShowEntry>, List<WatchedShowEntry>>(
    reader = { watchedShowsDao.entriesObservable() },
    writer = { _: Unit, response ->
      val syncer = syncerForEntity(
        entityDao = watchedShowsDao,
        entityToKey = { it.showId },
        mapper = { newEntity, currentEntity ->
          newEntity.copy(id = currentEntity?.id ?: 0)
        },
        logger = logger,
      )
      transactionRunner {
        syncer.sync(
          currentValues = watchedShowsDao.entries(),
          networkValues = response,
        )
      }
    },
    delete = {
      // Delete of an entity here means the entire list
      transactionRunner(watchedShowsDao::deleteAll)
    },
    deleteAll = { transactionRunner(watchedShowsDao::deleteAll) },
  ).usingDispatchers(
    readDispatcher = dispatchers.databaseRead,
    writeDispatcher = dispatchers.databaseWrite,
  ),
).validator(
  Validator.by { result ->
    withContext(dispatchers.io) {
      lastRequestStore.isRequestValid(
        threshold = if (result.isNotEmpty()) 6.hours else 30.minutes,
      )
    }
  },
).build()
