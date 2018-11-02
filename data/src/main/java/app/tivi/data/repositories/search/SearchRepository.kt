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

package app.tivi.data.repositories.search

import app.tivi.data.entities.SearchResults
import app.tivi.data.entities.Success
import app.tivi.data.repositories.shows.LocalShowStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val localSearchStore: LocalSearchStore,
    private val localShowStore: LocalShowStore,
    private val tmdbDataSource: TmdbSearchDataSource
) {
    suspend fun search(query: String): SearchResults {
        if (query.isEmpty()) {
            return SearchResults(query, emptyList())
        }

        val cacheValues = localSearchStore.getResults(query)
        if (cacheValues != null) {
            return SearchResults(query, cacheValues.map { localShowStore.getShow(it)!! })
        }

        // We need to hit TMDb instead
        val tmdbResult = tmdbDataSource.search(query)

        val results = when (tmdbResult) {
            is Success -> tmdbResult.data.map {
                val id = localShowStore.getIdOrSavePlaceholder(it)
                localShowStore.getShow(id)!!
            }.also { results ->
                // We need to save the search results
                localSearchStore.setResults(query, results.map { it.id }.toLongArray())
            }
            else -> emptyList()
        }
        return SearchResults(query, results)
    }
}