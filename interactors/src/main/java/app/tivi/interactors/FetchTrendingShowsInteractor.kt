/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.interactors

import app.tivi.ShowFetcher
import app.tivi.api.ItemWithIndex
import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.TrendingDao
import app.tivi.data.entities.TrendingShowEntry
import app.tivi.extensions.fetchBodyWithRetry
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Shows
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Provider

class FetchTrendingShowsInteractor @Inject constructor(
    databaseTransactionRunner: DatabaseTransactionRunner,
    private val trendingDao: TrendingDao,
    private val lastRequests: LastRequestDao,
    private val showFetcher: ShowFetcher,
    private val showsService: Provider<Shows>,
    dispatchers: AppCoroutineDispatchers,
    logger: Logger
) : Interactor<FetchTrendingShowsInteractor.Params> {
    private val pageSize: Int = 21
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    private val helper = PagedInteractorHelper(
            databaseTransactionRunner,
            trendingDao,
            lastRequests,
            showFetcher,
            dispatchers,
            logger,
            { entity, showId, page -> TrendingShowEntry(showId = showId, page = page, watchers = entity.item.watchers) },
            { response -> showFetcher.insertPlaceholderIfNeeded(response.item.show) },
            { page ->
                showsService.get().trending(page + 1, pageSize, Extended.NOSEASONS)
                        .fetchBodyWithRetry()
                        .mapIndexed { index, show -> ItemWithIndex(show, index) }
            }
    )

    override suspend fun invoke(param: Params) {
        if (param.page == Params.NEXT_PAGE) {
            helper.loadPage(trendingDao.getLastPage() + 1, false)
        } else {
            helper.loadPage(param.page, resetOnSave = param.page == Params.REFRESH)
        }
    }

    data class Params(val page: Int) {
        companion object {
            const val NEXT_PAGE = -1
            const val REFRESH = 0
        }
    }
}
