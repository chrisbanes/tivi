// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.popular

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.rememberRetainedCachedPagingFlow
import app.tivi.domain.observers.ObservePagedPopularShows
import app.tivi.screens.PopularShowsScreen
import app.tivi.screens.ShowDetailsScreen
import com.slack.circuit.retained.rememberRetained
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
    // Yes, this is gross. We need the same flow instance across Presenter instances. We could
    // make the interactor have @ApplicationScope, but that has other consequences if we use the
    // same interactor at the same time across UIs. Instead we just retain the instance
    val retainedPagingInteractor = rememberRetained { pagingInteractor.value }

    val items = retainedPagingInteractor.flow
      .rememberRetainedCachedPagingFlow()
      .collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
      retainedPagingInteractor(ObservePagedPopularShows.Params(PAGING_CONFIG))
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
