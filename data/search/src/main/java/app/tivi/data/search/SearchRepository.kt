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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val showTmdbImagesDao: ShowTmdbImagesDao,
    private val showDao: TiviShowDao,
    private val tmdbDataSource: TmdbSearchDataSource,
) {
    private val cache by lazy { LruCache<String, LongArray>(32) }

    suspend fun search(query: String): List<ShowDetailed> {
        if (query.isBlank()) {
            return emptyList()
        }

        val cacheValues = cache[query]
        if (cacheValues != null) {
            return cacheValues.map { showDao.getShowWithIdDetailed(it)!! }
        }

        // We need to hit TMDb instead
        return try {
            val tmdbResult = tmdbDataSource.search(query)
            tmdbResult.map { (show, images) ->
                val showId = showDao.getIdOrSavePlaceholder(show)
                if (images.isNotEmpty()) {
                    showTmdbImagesDao.saveImagesIfEmpty(showId, images.map { it.copy(showId = showId) })
                }
                showId
            }.also { results ->
                // We need to save the search results
                cache.put(query, results.toLongArray())
            }.mapNotNull {
                // Finally map back to a TiviShow instance
                showDao.getShowWithIdDetailed(it)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
