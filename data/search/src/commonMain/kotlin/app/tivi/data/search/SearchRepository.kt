// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.search

import app.tivi.data.daos.ShowTmdbImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.daos.saveImagesIfEmpty
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.TiviShow
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.cancellableRunCatching
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class SearchRepository(
  private val showTmdbImagesDao: ShowTmdbImagesDao,
  private val showDao: TiviShowDao,
  private val tmdbDataSource: SearchDataSource,
  private val transactionRunner: DatabaseTransactionRunner,
  private val dispatchers: AppCoroutineDispatchers,
) {
  private val cache by lazy { mutableMapOf<String, List<Long>>() }

  suspend fun search(query: String): List<TiviShow> {
    if (query.isBlank()) {
      return emptyList()
    }

    val cacheValues = cache[query]
    if (cacheValues != null) {
      return cacheValues
        .mapNotNull { showDao.getShowWithId(it) }
    }

    // We need to hit TMDb
    val remoteResult = cancellableRunCatching {
      fetchFromTmdb(query)
        .also { results ->
          // We need to save the search results
          cache[query] = results
        }
        .mapNotNull { showDao.getShowWithId(it) }
    }
    return remoteResult.getOrDefault(emptyList())
  }

  private suspend fun fetchFromTmdb(query: String): List<Long> {
    return tmdbDataSource.search(query)
      .map { (show, images) ->
        withContext(dispatchers.databaseWrite) {
          transactionRunner {
            val showId = showDao.getIdOrSavePlaceholder(show)
            if (images.isNotEmpty()) {
              showTmdbImagesDao.saveImagesIfEmpty(
                showId = showId,
                images = images.map { it.copy(showId = showId) },
              )
            }
            showId
          }
        }
      }
  }
}
