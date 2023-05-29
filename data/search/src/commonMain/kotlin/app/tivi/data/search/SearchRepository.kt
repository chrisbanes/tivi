// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.search

import androidx.collection.LruCache
import app.tivi.data.daos.ShowTmdbImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.daos.saveImagesIfEmpty
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.TiviShow
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class SearchRepository(
    private val showTmdbImagesDao: ShowTmdbImagesDao,
    private val showDao: TiviShowDao,
    private val tmdbDataSource: SearchDataSource,
    private val transactionRunner: DatabaseTransactionRunner,
) {
    private val cache by lazy { LruCache<String, List<Long>>(20) }

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
        val remoteResult = runCatching {
            fetchFromTmdb(query)
                .also { results ->
                    // We need to save the search results
                    cache.put(query, results)
                }
                .mapNotNull { showDao.getShowWithId(it) }
        }
        return remoteResult.getOrDefault(emptyList())
    }

    private suspend fun fetchFromTmdb(query: String): List<Long> {
        return tmdbDataSource.search(query)
            .map { (show, images) ->
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
