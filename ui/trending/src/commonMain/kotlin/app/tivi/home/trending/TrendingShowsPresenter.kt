// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.trending

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.cash.paging.PagingConfig
import app.cash.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.rememberCachedPagingFlow
import app.tivi.domain.observers.ObservePagedTrendingShows
import app.tivi.screens.ShowDetailsScreen
import app.tivi.screens.TrendingShowsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
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
  private val pagingInteractor: Lazy<ObservePagedTrendingShows>,
) : Presenter<TrendingShowsUiState> {

  @Composable
  override fun present(): TrendingShowsUiState {
    val items = pagingInteractor.value.flow
      .rememberCachedPagingFlow()
      .collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
      pagingInteractor.value.invoke(ObservePagedTrendingShows.Params(PAGING_CONFIG))
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
