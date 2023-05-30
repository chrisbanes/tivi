// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.upnext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.api.UiMessage
import app.tivi.api.UiMessageManager
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.data.models.SortOption
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.executeSync
import app.tivi.domain.interactors.GetTraktAuthState
import app.tivi.domain.interactors.UpdateUpNextEpisodes
import app.tivi.domain.observers.ObservePagedUpNextShows
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.screens.AccountScreen
import app.tivi.screens.EpisodeDetailsScreen
import app.tivi.screens.ShowDetailsScreen
import app.tivi.screens.ShowSeasonsScreen
import app.tivi.screens.UpNextScreen
import app.tivi.settings.TiviPreferences
import app.tivi.util.Logger
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
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
    private val observePagedUpNextShows: ObservePagedUpNextShows,
    private val updateUpNextEpisodes: UpdateUpNextEpisodes,
    private val observeTraktAuthState: ObserveTraktAuthState,
    private val observeUserDetails: ObserveUserDetails,
    private val getTraktAuthState: GetTraktAuthState,
    private val preferences: TiviPreferences,
    private val logger: Logger,
) : Presenter<UpNextUiState> {

    @Composable
    override fun present(): UpNextUiState {
        val scope = rememberCoroutineScope()

        val uiMessageManager = remember { UiMessageManager() }

        val items = observePagedUpNextShows.flow.collectAsLazyPagingItems()

        var sort by remember { mutableStateOf(SortOption.LAST_WATCHED) }

        val loading by updateUpNextEpisodes.inProgress.collectAsState(false)
        val message by uiMessageManager.message.collectAsState(null)

        val user by observeUserDetails.flow.collectAsState(null)
        val authState by observeTraktAuthState.flow.collectAsState(TraktAuthState.LOGGED_OUT)

        val followedShowsOnly by produceState(false, preferences) {
            preferences.observeUpNextFollowedOnly().collect { value = it }
        }

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
                        if (getTraktAuthState.executeSync() == TraktAuthState.LOGGED_IN) {
                            updateUpNextEpisodes(
                                UpdateUpNextEpisodes.Params(event.fromUser),
                            ).also { result ->
                                result.exceptionOrNull()?.let { e ->
                                    logger.i(e)
                                    uiMessageManager.emitMessage(UiMessage(e))
                                }
                            }
                        }
                    }
                }

                UpNextUiEvent.ToggleFollowedShowsOnly -> {
                    preferences.upNextFollowedOnly = !preferences.upNextFollowedOnly
                }

                UpNextUiEvent.OpenAccount -> navigator.goTo(AccountScreen)
                is UpNextUiEvent.OpenShowDetails -> {
                    navigator.goTo(ShowDetailsScreen(event.showId))
                    navigator.goTo(ShowSeasonsScreen(event.showId, event.seasonId))
                    navigator.goTo(EpisodeDetailsScreen(event.episodeId))
                }
            }
        }

        LaunchedEffect(Unit) {
            observeTraktAuthState(Unit)
            observeUserDetails(ObserveUserDetails.Params("me"))
        }

        LaunchedEffect(observeTraktAuthState) {
            observeTraktAuthState.flow
                .filter { it == TraktAuthState.LOGGED_IN }
                .collect {
                    eventSink(UpNextUiEvent.Refresh(false))
                }
        }

        LaunchedEffect(sort, followedShowsOnly) {
            // When the filter and sort options change, update the data source
            observePagedUpNextShows(
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
