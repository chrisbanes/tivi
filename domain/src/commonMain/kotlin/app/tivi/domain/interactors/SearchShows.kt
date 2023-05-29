// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.models.TiviShow
import app.tivi.data.search.SearchRepository
import app.tivi.domain.SuspendingWorkInteractor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class SearchShows(
    private val searchRepository: SearchRepository,
    private val showDao: TiviShowDao,
    private val dispatchers: AppCoroutineDispatchers,
) : SuspendingWorkInteractor<SearchShows.Params, List<TiviShow>>() {
    override suspend fun doWork(params: Params): List<TiviShow> = withContext(dispatchers.io) {
        val remoteResults = searchRepository.search(params.query)
        when {
            remoteResults.isNotEmpty() -> remoteResults
            params.query.isNotBlank() -> {
                try {
                    showDao.search("%${params.query}%")
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
