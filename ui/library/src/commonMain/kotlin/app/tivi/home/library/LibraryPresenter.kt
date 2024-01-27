// Copyright 2022, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.paging.PagingConfig
import app.cash.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.UiMessage
import app.tivi.common.compose.UiMessageManager
import app.tivi.common.compose.rememberCachedPagingFlow
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.data.models.SortOption
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.interactors.GetTraktAuthState
import app.tivi.domain.interactors.UpdateLibraryShows
import app.tivi.domain.invoke
import app.tivi.domain.observers.ObservePagedLibraryShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.screens.AccountScreen
import app.tivi.screens.LibraryScreen
import app.tivi.screens.ShowDetailsScreen
import app.tivi.settings.TiviPreferences
import app.tivi.util.Logger
import app.tivi.util.onException
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class LibraryUiPresenterFactory(
  private val presenterFactory: (Navigator) -> LibraryPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is LibraryScreen -> presenterFactory(navigator)
    else -> null
  }
}

@Inject
class LibraryPresenter(
  @Assisted private val navigator: Navigator,
  private val updateLibraryShows: Lazy<UpdateLibraryShows>,
  private val observePagedLibraryShows: Lazy<ObservePagedLibraryShows>,
  private val observeTraktAuthState: Lazy<ObserveTraktAuthState>,
  private val observeUserDetails: Lazy<ObserveUserDetails>,
  private val getTraktAuthState: Lazy<GetTraktAuthState>,
  private val preferences: Lazy<TiviPreferences>,
  private val logger: Logger,
) : Presenter<LibraryUiState> {

  @Composable
  override fun present(): LibraryUiState {
    val scope = rememberCoroutineScope()
    val uiMessageManager = remember { UiMessageManager() }

    val items = observePagedLibraryShows.value.flow
      .rememberCachedPagingFlow(scope)
      .collectAsLazyPagingItems()

    var filter by remember { mutableStateOf<String?>(null) }
    var sort by remember { mutableStateOf(SortOption.LAST_WATCHED) }

    val loading by updateLibraryShows.value.inProgress.collectAsState(false)
    val message by uiMessageManager.message.collectAsState(null)

    val user by observeUserDetails.value.flow.collectAsRetainedState(null)
    val authState by observeTraktAuthState.value.flow.collectAsRetainedState(TraktAuthState.LOGGED_OUT)

    val includeWatchedShows by remember { preferences.value.observeLibraryWatchedActive() }
      .collectAsRetainedState(false)
    val includeFollowedShows by remember { preferences.value.observeLibraryFollowedActive() }
      .collectAsRetainedState(false)

    fun eventSink(event: LibraryUiEvent) {
      when (event) {
        is LibraryUiEvent.ChangeFilter -> filter = event.filter
        is LibraryUiEvent.ChangeSort -> sort = event.sort
        is LibraryUiEvent.ClearMessage -> {
          scope.launch {
            uiMessageManager.clearMessage(event.id)
          }
        }
        is LibraryUiEvent.Refresh -> {
          scope.launch {
            if (getTraktAuthState.value.invoke().getOrThrow() == TraktAuthState.LOGGED_IN) {
              updateLibraryShows.value.invoke(
                UpdateLibraryShows.Params(event.fromUser),
              ).onException { e ->
                logger.i(e)
                uiMessageManager.emitMessage(UiMessage(e))
              }
            }
          }
        }
        LibraryUiEvent.ToggleFollowedShowsIncluded -> {
          preferences.value.libraryFollowedActive = !preferences.value.libraryFollowedActive
        }
        LibraryUiEvent.ToggleWatchedShowsIncluded -> {
          preferences.value.libraryWatchedActive = !preferences.value.libraryWatchedActive
        }
        LibraryUiEvent.OpenAccount -> navigator.goTo(AccountScreen)
        is LibraryUiEvent.OpenShowDetails -> {
          navigator.goTo(ShowDetailsScreen(event.showId))
        }
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
          eventSink(LibraryUiEvent.Refresh(false))
        }
    }

    LaunchedEffect(filter, sort, includeFollowedShows, includeWatchedShows) {
      // When the filter and sort options change, update the data source
      observePagedLibraryShows.value.invoke(
        ObservePagedLibraryShows.Parameters(
          sort = sort,
          filter = filter,
          includeFollowed = preferences.value.libraryFollowedActive,
          includeWatched = preferences.value.libraryWatchedActive,
          pagingConfig = PAGING_CONFIG,
        ),
      )
    }

    return LibraryUiState(
      items = items,
      user = user,
      authState = authState,
      isLoading = loading,
      filter = filter,
      filterActive = !filter.isNullOrEmpty(),
      availableSorts = AVAILABLE_SORT_OPTIONS,
      sort = sort,
      message = message,
      watchedShowsIncluded = includeWatchedShows,
      followedShowsIncluded = includeFollowedShows,
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
      SortOption.ALPHABETICAL,
    )
  }
}
