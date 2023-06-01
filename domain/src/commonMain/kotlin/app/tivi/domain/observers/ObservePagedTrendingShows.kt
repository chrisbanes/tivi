// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.tivi.data.compoundmodels.TrendingEntryWithShow
import app.tivi.data.daos.TrendingDao
import app.tivi.domain.PaginatedEntryRemoteMediator
import app.tivi.domain.PagingInteractor
import app.tivi.domain.interactors.UpdateTrendingShows
import app.tivi.util.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObservePagedTrendingShows(
    private val trendingShowsDao: TrendingDao,
    private val updateTrendingShows: UpdateTrendingShows,
    private val logger: Logger,
) : PagingInteractor<ObservePagedTrendingShows.Params, TrendingEntryWithShow>() {
    @OptIn(app.cash.paging.ExperimentalPagingApi::class)
    override fun createObservable(
        params: Params,
    ): Flow<PagingData<TrendingEntryWithShow>> {
        return Pager(
            config = params.pagingConfig,
            remoteMediator = PaginatedEntryRemoteMediator { page ->
                try {
                    updateTrendingShows(
                        UpdateTrendingShows.Params(page = page, forceRefresh = true),
                    )
                } catch (ce: CancellationException) {
                    throw ce
                } catch (t: Throwable) {
                    logger.e(t) { "Error while fetching from RemoteMediator" }
                    throw t
                }
            },
            pagingSourceFactory = trendingShowsDao::entriesPagingSource,
        ).flow
    }

    data class Params(
        override val pagingConfig: PagingConfig,
    ) : Parameters<TrendingEntryWithShow>
}
