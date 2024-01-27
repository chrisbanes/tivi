// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.showimages

import app.tivi.data.daos.ShowTmdbImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.saveImages
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.ShowImages
import app.tivi.data.util.storeBuilder
import app.tivi.data.util.usingDispatchers
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.Validator

@ApplicationScope
@Inject
class ShowImagesStore(
  showTmdbImagesDao: ShowTmdbImagesDao,
  showDao: TiviShowDao,
  lastRequestStore: ShowImagesLastRequestStore,
  dataSource: ShowImagesDataSource,
  transactionRunner: DatabaseTransactionRunner,
  dispatchers: AppCoroutineDispatchers,
) : Store<Long, ShowImages> by storeBuilder(
  fetcher = Fetcher.of { showId: Long ->
    val show = showDao.getShowWithId(showId)
    if (show?.tmdbId != null) {
      dataSource.getShowImages(show)
        .map { it.copy(showId = showId) }
        .let { ShowImages(showId, it) }
        .also { lastRequestStore.updateLastRequest(showId) }
    } else {
      ShowImages(showId, emptyList())
    }
  },
  sourceOfTruth = SourceOfTruth.of<Long, ShowImages, ShowImages>(
    reader = { showId ->
      showTmdbImagesDao.getImagesForShowId(showId).map { ShowImages(showId, it) }
    },
    writer = { showId, images ->
      transactionRunner {
        showTmdbImagesDao.saveImages(showId, images.images)
      }
    },
    delete = showTmdbImagesDao::deleteForShowId,
    deleteAll = { transactionRunner(showTmdbImagesDao::deleteAll) },
  ).usingDispatchers(
    readDispatcher = dispatchers.databaseRead,
    writeDispatcher = dispatchers.databaseWrite,
  ),
).validator(
  Validator.by { result ->
    withContext(dispatchers.io) {
      lastRequestStore.isRequestValid(
        entityId = result.showId,
        threshold = if (result.images.isNotEmpty()) 180.days else 1.hours,
      )
    }
  },
).build()
