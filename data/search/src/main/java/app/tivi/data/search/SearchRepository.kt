/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.data.search

import androidx.collection.LruCache
import app.tivi.data.compoundmodels.ShowDetailed
import app.tivi.data.daos.ShowTmdbImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.daos.saveImagesIfEmpty
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class SearchRepository(
    private val showTmdbImagesDao: ShowTmdbImagesDao,
    private val showDao: TiviShowDao,
    private val tmdbDataSource: SearchDataSource,
) {
    private val cache by lazy { LruCache<String, List<Long>>(20) }

    suspend fun search(query: String): List<ShowDetailed> {
        if (query.isBlank()) {
            return emptyList()
        }

        val cacheValues = cache[query]
        if (cacheValues != null) {
            return cacheValues
                .mapNotNull { showDao.getShowWithIdDetailed(it) }
        }

        // We need to hit TMDb
        val remoteResult = runCatching {
            fetchFromTmdb(query)
                .also { results ->
                    // We need to save the search results
                    cache.put(query, results)
                }
                .mapNotNull { showDao.getShowWithIdDetailed(it) }
        }
        return remoteResult.getOrDefault(emptyList())
    }

    private suspend fun fetchFromTmdb(query: String): List<Long> {
        return tmdbDataSource.search(query)
            .map { (show, images) ->
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
