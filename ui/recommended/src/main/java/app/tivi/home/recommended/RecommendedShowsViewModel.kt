// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.recommended

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.domain.observers.ObservePagedRecommendedShows
import me.tatarka.inject.annotations.Inject

@Inject
class RecommendedShowsViewModel(
    private val pagingInteractor: ObservePagedRecommendedShows,
) : ViewModel() {

    @Composable
    fun presenter(): RecommendedShowsViewState {
        val items = pagingInteractor.flow.collectAsLazyPagingItems()

        LaunchedEffect(Unit) {
            pagingInteractor(ObservePagedRecommendedShows.Params(PAGING_CONFIG))
        }

        return RecommendedShowsViewState(
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
