// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.tivi.data.compoundmodels.RecommendedEntryWithShow
import app.tivi.data.daos.RecommendedDao
import app.tivi.domain.PagingInteractor
import app.tivi.domain.RefreshOnlyRemoteMediator
import app.tivi.domain.interactors.UpdateRecommendedShows
import app.tivi.util.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObservePagedRecommendedShows(
    private val recommendedShowsDao: RecommendedDao,
    private val updateRecommendedShows: UpdateRecommendedShows,
    private val logger: Logger,
) : PagingInteractor<ObservePagedRecommendedShows.Params, RecommendedEntryWithShow>() {
    @OptIn(app.cash.paging.ExperimentalPagingApi::class)
    override fun createObservable(
        params: Params,
    ): Flow<PagingData<RecommendedEntryWithShow>> {
        return Pager(
            config = params.pagingConfig,
            remoteMediator = RefreshOnlyRemoteMediator {
                try {
                    updateRecommendedShows(UpdateRecommendedShows.Params(forceRefresh = true))
                } catch (ce: CancellationException) {
                    throw ce
                } catch (t: Throwable) {
                    logger.e(t) { "Error while fetching from RemoteMediator" }
                    throw t
                }
            },
            pagingSourceFactory = recommendedShowsDao::entriesPagingSource,
        ).flow
    }

    data class Params(
        override val pagingConfig: PagingConfig,
    ) : Parameters<RecommendedEntryWithShow>
}
