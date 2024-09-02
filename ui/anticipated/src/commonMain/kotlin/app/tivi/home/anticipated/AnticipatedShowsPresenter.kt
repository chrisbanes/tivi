// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.anticipated

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.rememberRetainedCachedPagingFlow
import app.tivi.domain.observers.ObservePagedAnticipatedShows
import app.tivi.screens.AnticipatedShowsScreen
import app.tivi.screens.ShowDetailsScreen
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class AnticipatedShowsUiPresenterFactory(
  private val presenterFactory: (Navigator) -> AnticipatedShowsPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is AnticipatedShowsScreen -> presenterFactory(navigator)
    else -> null
  }
}

@Inject
class AnticipatedShowsPresenter(
  @Assisted private val navigator: Navigator,
  private val pagingInteractor: Lazy<ObservePagedAnticipatedShows>,
) : Presenter<AnticipatedShowsUiState> {

  @Composable
  override fun present(): AnticipatedShowsUiState {
    // Yes, this is gross. We need the same flow instance across Presenter instances. We could
    // make the interactor have @ApplicationScope, but that has other consequences if we use the
    // same interactor at the same time across UIs. Instead we just retain the instance
    val retainedPagingInteractor = rememberRetained { pagingInteractor.value }

    val items = retainedPagingInteractor.flow
      .rememberRetainedCachedPagingFlow()
      .collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
      retainedPagingInteractor(ObservePagedAnticipatedShows.Params(PAGING_CONFIG))
    }

    fun eventSink(event: AnticipatedShowsUiEvent) {
      when (event) {
        AnticipatedShowsUiEvent.NavigateUp -> navigator.pop()
        is AnticipatedShowsUiEvent.OpenShowDetails -> {
          navigator.goTo(ShowDetailsScreen(event.showId))
        }
      }
    }

    return AnticipatedShowsUiState(
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
