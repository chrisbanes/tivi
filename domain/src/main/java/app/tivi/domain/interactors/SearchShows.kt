/*
 * Copyright 2019 Google LLC
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

package app.tivi.domain.interactors

import app.tivi.data.daos.ShowFtsDao
import app.tivi.data.repositories.search.SearchRepository
import app.tivi.data.resultentities.ShowDetailed
import app.tivi.domain.SuspendingWorkInteractor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchShows @Inject constructor(
    private val searchRepository: SearchRepository,
    private val showFtsDao: ShowFtsDao,
    private val dispatchers: AppCoroutineDispatchers
) : SuspendingWorkInteractor<SearchShows.Params, List<ShowDetailed>>() {
    override suspend fun doWork(params: Params): List<ShowDetailed> {
        return withContext(dispatchers.io) {
            val remoteResults = searchRepository.search(params.query)
            if (remoteResults.isNotEmpty()) {
                remoteResults
            } else {
                when {
                    params.query.isNotBlank() -> {
                        try {
                            showFtsDao.search("*$params.query*")
                        } catch (e: Exception) {
                            // Re-throw wrapped exception with the query
                            throw IllegalArgumentException(
                                "Error while searching database with query: ${params.query}",
                                e,
                            )
                        }
                    }
                    else -> emptyList()
                }
            }
        }
    }

    data class Params(val query: String)
}
