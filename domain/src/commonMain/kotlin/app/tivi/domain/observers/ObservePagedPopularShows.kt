// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.tivi.data.compoundmodels.PopularEntryWithShow
import app.tivi.data.daos.PopularDao
import app.tivi.domain.PaginatedEntryRemoteMediator
import app.tivi.domain.PagingInteractor
import app.tivi.domain.interactors.UpdatePopularShows
import app.tivi.util.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObservePagedPopularShows(
    private val popularDao: PopularDao,
    private val updatePopularShows: UpdatePopularShows,
    private val logger: Logger,
) : PagingInteractor<ObservePagedPopularShows.Params, PopularEntryWithShow>() {
    override fun createObservable(
        params: Params,
    ): Flow<PagingData<PopularEntryWithShow>> {
        return Pager(
            config = params.pagingConfig,
            remoteMediator = PaginatedEntryRemoteMediator { page ->
                try {
                    updatePopularShows(UpdatePopularShows.Params(page = page, forceRefresh = true))
                } catch (ce: CancellationException) {
                    throw ce
                } catch (t: Throwable) {
                    logger.e(t, "Error while fetching from RemoteMediator")
                    throw t
                }
            },
            pagingSourceFactory = popularDao::entriesPagingSource,
        ).flow
    }

    data class Params(
        override val pagingConfig: PagingConfig,
    ) : Parameters<PopularEntryWithShow>
}
