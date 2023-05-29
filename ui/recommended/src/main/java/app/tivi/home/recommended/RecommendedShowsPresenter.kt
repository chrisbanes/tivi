// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.recommended

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.domain.observers.ObservePagedRecommendedShows
import app.tivi.screens.RecommendedShowsScreen
import app.tivi.screens.ShowDetailsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class RecommendedShowsUiPresenterFactory(
    private val presenterFactory: (Navigator) -> RecommendedShowsPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? = when (screen) {
        is RecommendedShowsScreen -> presenterFactory(navigator)
        else -> null
    }
}

@Inject
class RecommendedShowsPresenter(
    @Assisted private val navigator: Navigator,
    private val pagingInteractor: ObservePagedRecommendedShows,
) : Presenter<RecommendedShowsUiState> {

    @Composable
    override fun present(): RecommendedShowsUiState {
        val items = pagingInteractor.flow.collectAsLazyPagingItems()

        LaunchedEffect(Unit) {
            pagingInteractor(ObservePagedRecommendedShows.Params(PAGING_CONFIG))
        }

        fun eventSink(event: RecommendedShowsUiEvent) {
            when (event) {
                RecommendedShowsUiEvent.NavigateUp -> navigator.pop()
                is RecommendedShowsUiEvent.OpenShowDetails -> {
                    navigator.goTo(ShowDetailsScreen(event.showId))
                }
            }
        }

        return RecommendedShowsUiState(
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
