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
import app.tivi.data.models.TiviShow
import app.tivi.data.search.SearchRepository
import app.tivi.domain.SuspendingWorkInteractor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class SearchShows(
    private val searchRepository: SearchRepository,
    private val showFtsDao: ShowFtsDao,
    private val dispatchers: AppCoroutineDispatchers,
) : SuspendingWorkInteractor<SearchShows.Params, List<TiviShow>>() {
    override suspend fun doWork(params: Params): List<TiviShow> = withContext(dispatchers.io) {
        val remoteResults = searchRepository.search(params.query)
        when {
            remoteResults.isNotEmpty() -> remoteResults
            params.query.isNotBlank() -> {
                try {
                    showFtsDao.search("*$params.query*")
                } catch (ce: CancellationException) {
                    // Cancellation exceptions should be re-thrown
                    throw ce
                } catch (e: Exception) {
                    // Re-throw wrapped exception with the query
                    throw Exception("Error while searching database with query: ${params.query}", e)
                }
            }

            else -> emptyList()
        }
    }

    data class Params(val query: String)
}
