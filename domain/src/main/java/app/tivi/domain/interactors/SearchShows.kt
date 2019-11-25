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

import app.tivi.data.entities.SearchResults
import app.tivi.data.repositories.search.SearchRepository
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.domain.SuspendingWorkInteractor
import app.tivi.util.AppCoroutineDispatchers
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher

class SearchShows @Inject constructor(
    private val searchRepository: SearchRepository,
    private val showRepository: ShowRepository,
    dispatchers: AppCoroutineDispatchers
) : SuspendingWorkInteractor<SearchShows.Params, SearchResults>() {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend fun doWork(params: Params): SearchResults {
        val remoteResults = searchRepository.search(params.query)
        return when {
            remoteResults.isNotEmpty() -> SearchResults(params.query, remoteResults)
            else -> SearchResults(params.query, showRepository.searchShows(params.query))
        }
    }

    data class Params(val query: String)
}
