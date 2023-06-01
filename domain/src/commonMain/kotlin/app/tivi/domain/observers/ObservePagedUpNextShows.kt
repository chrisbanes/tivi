// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.models.SortOption
import app.tivi.domain.PagingInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObservePagedUpNextShows(
    private val watchedShowsDao: WatchedShowDao,
) : PagingInteractor<ObservePagedUpNextShows.Parameters, UpNextEntry>() {

    @OptIn(app.cash.paging.ExperimentalPagingApi::class)
    override fun createObservable(
        params: Parameters,
    ): Flow<PagingData<UpNextEntry>> = Pager(config = params.pagingConfig) {
        watchedShowsDao.pagedUpNextShows(
            followedOnly = params.followedOnly,
            sort = params.sort,
        )
    }.flow

    data class Parameters(
        val sort: SortOption,
        val followedOnly: Boolean,
        override val pagingConfig: PagingConfig,
    ) : PagingInteractor.Parameters<UpNextEntry>
}
