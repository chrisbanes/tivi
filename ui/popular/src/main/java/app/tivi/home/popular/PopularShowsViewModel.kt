// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.popular

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.domain.observers.ObservePagedPopularShows
import me.tatarka.inject.annotations.Inject

@Inject
class PopularShowsViewModel(
    private val pagingInteractor: ObservePagedPopularShows,
) : ViewModel() {

    @Composable
    fun presenter(): PopularViewState {
        val items = pagingInteractor.flow.collectAsLazyPagingItems()

        LaunchedEffect(Unit) {
            pagingInteractor(ObservePagedPopularShows.Params(PAGING_CONFIG))
        }

        return PopularViewState(
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
