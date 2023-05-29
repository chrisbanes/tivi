// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.daos.PopularDao
import app.tivi.data.popularshows.PopularShowsStore
import app.tivi.data.shows.ShowStore
import app.tivi.data.util.fetch
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdatePopularShows.Params
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.parallelForEach
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdatePopularShows(
    private val popularShowStore: PopularShowsStore,
    private val popularDao: PopularDao,
    private val showStore: ShowStore,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            val page = when {
                params.page >= 0 -> params.page
                params.page == UpdateTrendingShows.Page.NEXT_PAGE -> {
                    val lastPage = popularDao.getLastPage()
                    if (lastPage != null) lastPage + 1 else 0
                }
                else -> 0
            }

            popularShowStore.fetch(page, forceFresh = params.forceRefresh).parallelForEach {
                showStore.fetch(it.showId)
            }
        }
    }

    data class Params(val page: Int, val forceRefresh: Boolean = false)

    object Page {
        const val NEXT_PAGE = -1
        const val REFRESH = -2
    }
}
