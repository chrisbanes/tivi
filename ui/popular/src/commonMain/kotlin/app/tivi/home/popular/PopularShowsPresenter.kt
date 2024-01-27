// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.popular

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.cash.paging.PagingConfig
import app.cash.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.rememberCachedPagingFlow
import app.tivi.domain.observers.ObservePagedPopularShows
import app.tivi.screens.PopularShowsScreen
import app.tivi.screens.ShowDetailsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class PopularShowsUiPresenterFactory(
  private val presenterFactory: (Navigator) -> PopularShowsPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is PopularShowsScreen -> presenterFactory(navigator)
    else -> null
  }
}

@Inject
class PopularShowsPresenter(
  @Assisted private val navigator: Navigator,
  private val pagingInteractor: Lazy<ObservePagedPopularShows>,
) : Presenter<PopularShowsUiState> {

  @Composable
  override fun present(): PopularShowsUiState {
    val items = pagingInteractor.value.flow
      .rememberCachedPagingFlow()
      .collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
      pagingInteractor.value.invoke(ObservePagedPopularShows.Params(PAGING_CONFIG))
    }

    fun eventSink(event: PopularShowsUiEvent) {
      when (event) {
        PopularShowsUiEvent.NavigateUp -> navigator.pop()
        is PopularShowsUiEvent.OpenShowDetails -> {
          navigator.goTo(ShowDetailsScreen(event.showId))
        }
      }
    }

    return PopularShowsUiState(
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
