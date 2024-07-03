// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.upnext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.UiMessage
import app.tivi.common.compose.UiMessageManager
import app.tivi.common.compose.collectAsState
import app.tivi.common.compose.rememberRetainedCachedPagingFlow
import app.tivi.data.models.SortOption
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.interactors.GetTraktAuthState
import app.tivi.domain.interactors.UpdateUpNextEpisodes
import app.tivi.domain.invoke
import app.tivi.domain.observers.ObservePagedUpNextShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.screens.AccountScreen
import app.tivi.screens.EpisodeDetailsScreen
import app.tivi.screens.UpNextScreen
import app.tivi.settings.TiviPreferences
import app.tivi.settings.toggle
import app.tivi.util.Logger
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class UpNextUiPresenterFactory(
  private val presenterFactory: (Navigator) -> UpNextPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is UpNextScreen -> presenterFactory(navigator)
    else -> null
  }
}

@Inject
class UpNextPresenter(
  @Assisted private val navigator: Navigator,
  private val observePagedUpNextShows: Lazy<ObservePagedUpNextShows>,
  private val updateUpNextEpisodes: Lazy<UpdateUpNextEpisodes>,
  private val observeTraktAuthState: Lazy<ObserveTraktAuthState>,
  private val observeUserDetails: Lazy<ObserveUserDetails>,
  private val getTraktAuthState: Lazy<GetTraktAuthState>,
  private val preferences: Lazy<TiviPreferences>,
  private val logger: Logger,
) : Presenter<UpNextUiState> {

  @Composable
  override fun present(): UpNextUiState {
    val scope = rememberCoroutineScope()

    val uiMessageManager = remember { UiMessageManager() }

    // Yes, this is gross. We need the same flow instance across Presenter instances. We could
    // make the interactor have @ApplicationScope, but that has other consequences if we use the
    // same interactor at the same time across UIs. Instead we just retain the instance
    val retainedObservePagedUpNextShows = rememberRetained { observePagedUpNextShows.value }

    val items = retainedObservePagedUpNextShows.flow
      .rememberRetainedCachedPagingFlow()
      .collectAsLazyPagingItems()

    var sort by remember { mutableStateOf(SortOption.LAST_WATCHED) }

    val loading by updateUpNextEpisodes.value.inProgress.collectAsState(false)
    val message by uiMessageManager.message.collectAsState(null)

    val user by observeUserDetails.value.flow.collectAsRetainedState(null)
    val authState by observeTraktAuthState.value.flow.collectAsRetainedState(TraktAuthState.LOGGED_OUT)

    val followedShowsOnly by preferences.value.upNextFollowedOnly.collectAsState()

    fun eventSink(event: UpNextUiEvent) {
      when (event) {
        is UpNextUiEvent.ChangeSort -> sort = event.sort
        is UpNextUiEvent.ClearMessage -> {
          scope.launch {
            uiMessageManager.clearMessage(event.id)
          }
        }

        is UpNextUiEvent.Refresh -> {
          scope.launch {
            if (getTraktAuthState.value.invoke().getOrThrow() == TraktAuthState.LOGGED_IN) {
              updateUpNextEpisodes.value.invoke(
                UpdateUpNextEpisodes.Params(event.fromUser),
              ).onFailure { e ->
                logger.i(e)
                uiMessageManager.emitMessage(UiMessage(e))
              }
            }
          }
        }

        UpNextUiEvent.ToggleFollowedShowsOnly -> {
          scope.launch { preferences.value.upNextFollowedOnly.toggle() }
        }

        UpNextUiEvent.OpenAccount -> navigator.goTo(AccountScreen)

        is UpNextUiEvent.OpenEpisodeDetails -> navigator.goTo(EpisodeDetailsScreen(event.episodeId))
      }
    }

    LaunchedEffect(Unit) {
      observeTraktAuthState.value.invoke(Unit)
      observeUserDetails.value.invoke(ObserveUserDetails.Params("me"))
    }

    LaunchedEffect(observeTraktAuthState) {
      observeTraktAuthState.value.flow
        .filter { it == TraktAuthState.LOGGED_IN }
        .collect {
          eventSink(UpNextUiEvent.Refresh(false))
        }
    }

    LaunchedEffect(sort, followedShowsOnly) {
      // When the filter and sort options change, update the data source
      retainedObservePagedUpNextShows(
        ObservePagedUpNextShows.Parameters(
          sort = sort,
          followedOnly = followedShowsOnly,
          pagingConfig = PAGING_CONFIG,
        ),
      )
    }

    return UpNextUiState(
      items = items,
      user = user,
      authState = authState,
      isLoading = loading,
      availableSorts = AVAILABLE_SORT_OPTIONS,
      sort = sort,
      message = message,
      followedShowsOnly = followedShowsOnly,
      eventSink = ::eventSink,
    )
  }

  companion object {
    private val PAGING_CONFIG = PagingConfig(
      pageSize = 16,
      initialLoadSize = 32,
    )

    private val AVAILABLE_SORT_OPTIONS = listOf(
      SortOption.LAST_WATCHED,
      SortOption.AIR_DATE,
    )
  }
}
