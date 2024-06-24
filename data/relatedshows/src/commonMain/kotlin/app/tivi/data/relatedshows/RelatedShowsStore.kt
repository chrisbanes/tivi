// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.relatedshows

import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.RelatedShowEntry
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
class RelatedShowsStore(
  traktDataSource: TraktRelatedShowsDataSource,
  tmdbDataSource: TmdbRelatedShowsDataSource,
  relatedShowsDao: RelatedShowsDao,
  showDao: TiviShowDao,
  lastRequestStore: RelatedShowsLastRequestStore,
  transactionRunner: DatabaseTransactionRunner,
  dispatchers: AppCoroutineDispatchers,
) : Store<Long, RelatedShows> by storeBuilder(
  fetcher = Fetcher.of { showId: Long ->
    runCatching { tmdbDataSource(showId) }
      .let { tmdbResult ->
        when {
          tmdbResult.isSuccess -> tmdbResult
          else -> runCatching { traktDataSource(showId) }
        }
      }
      .getOrThrow()
      .let { result ->
        withContext(dispatchers.databaseWrite) {
          transactionRunner {
            lastRequestStore.updateLastRequest(showId)

            result.map { (show, entry) ->
              entry.copy(
                showId = showId,
                otherShowId = showDao.getIdOrSavePlaceholder(show),
              )
            }.distinctBy { it.otherShowId }
          }
        }
      }
      .let { RelatedShows(showId, it) }
  },
  sourceOfTruth = SourceOfTruth.of<Long, RelatedShows, RelatedShows>(
    reader = { showId ->
      relatedShowsDao.entriesObservable(showId)
        .map { RelatedShows(showId, it) }
    },
    writer = { showId, response ->
      transactionRunner {
        relatedShowsDao.deleteWithShowId(showId)
        relatedShowsDao.upsert(response.related)
      }
    },
    delete = relatedShowsDao::deleteWithShowId,
    deleteAll = { transactionRunner(relatedShowsDao::deleteAll) },
  ).usingDispatchers(
    readDispatcher = dispatchers.databaseRead,
    writeDispatcher = dispatchers.databaseWrite,
  ),
).validator(
  Validator.by { result ->
    withContext(dispatchers.io) {
      lastRequestStore.isRequestValid(
        entityId = result.showId,
        threshold = if (result.related.isNotEmpty()) 28.days else 1.hours,
      )
    }
  },
).build()

data class RelatedShows(val showId: Long, val related: List<RelatedShowEntry>)
