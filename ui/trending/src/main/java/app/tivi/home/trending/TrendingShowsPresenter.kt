// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.trending

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.domain.observers.ObservePagedTrendingShows
import app.tivi.screens.ShowDetailsScreen
import app.tivi.screens.TrendingShowsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class TrendingShowsUiPresenterFactory(
    private val presenterFactory: (Navigator) -> TrendingShowsPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? = when (screen) {
        is TrendingShowsScreen -> presenterFactory(navigator)
        else -> null
    }
}

@Inject
class TrendingShowsPresenter(
    @Assisted private val navigator: Navigator,
    private val pagingInteractor: ObservePagedTrendingShows,
) : Presenter<TrendingShowsUiState> {

    @Composable
    override fun present(): TrendingShowsUiState {
        val items = pagingInteractor.flow.collectAsLazyPagingItems()

        LaunchedEffect(Unit) {
            pagingInteractor(ObservePagedTrendingShows.Params(PAGING_CONFIG))
        }

        fun eventSink(event: TrendingShowsUiEvent) {
            when (event) {
                TrendingShowsUiEvent.NavigateUp -> navigator.pop()
                is TrendingShowsUiEvent.OpenShowDetails -> {
                    navigator.goTo(ShowDetailsScreen(event.showId))
                }
            }
        }

        return TrendingShowsUiState(
            items = items,
            eventSink = ::eventSink,
        )
    }

    companion object {
        val PAGING_CONFIG = PagingConfig(
            pageSize = 60,
            initialLoadSize = 60,
        )
    }
}
