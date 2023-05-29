// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.trending

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.domain.observers.ObservePagedTrendingShows
import me.tatarka.inject.annotations.Inject

@Inject
class TrendingShowsViewModel(
    private val pagingInteractor: ObservePagedTrendingShows,
) : ViewModel() {

    @Composable
    fun presenter(): TrendingShowsViewState {
        val items = pagingInteractor.flow.collectAsLazyPagingItems()

        LaunchedEffect(Unit) {
            pagingInteractor(ObservePagedTrendingShows.Params(PAGING_CONFIG))
        }

        return TrendingShowsViewState(
            items = items,
        )
    }

    companion object {
        val PAGING_CONFIG = PagingConfig(
            pageSize = 60,
            initialLoadSize = 60,
        )
    }
}
