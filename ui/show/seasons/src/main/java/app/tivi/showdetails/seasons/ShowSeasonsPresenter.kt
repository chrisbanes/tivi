// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.showdetails.seasons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.tivi.api.UiMessageManager
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.data.models.TiviShow
import app.tivi.domain.interactors.UpdateShowSeasons
import app.tivi.domain.observers.ObserveShowDetails
import app.tivi.domain.observers.ObserveShowSeasonsEpisodesWatches
import app.tivi.screens.EpisodeDetailsScreen
import app.tivi.screens.ShowSeasonsScreen
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import app.tivi.util.collectStatus
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ShowSeasonsUiPresenterFactory(
    private val presenterFactory: (ShowSeasonsScreen, Navigator) -> ShowSeasonsPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? = when (screen) {
        is ShowSeasonsScreen -> presenterFactory(screen, navigator)
        else -> null
    }
}

@Inject
class ShowSeasonsPresenter(
    @Assisted private val screen: ShowSeasonsScreen,
    @Assisted private val navigator: Navigator,
    private val observeShowDetails: ObserveShowDetails,
    private val observeShowSeasons: ObserveShowSeasonsEpisodesWatches,
    private val updateShowSeasons: UpdateShowSeasons,
    private val logger: Logger,
) : Presenter<ShowSeasonsUiState> {
    @Composable
    override fun present(): ShowSeasonsUiState {
        val scope = rememberCoroutineScope()

        val loadingState = remember { ObservableLoadingCounter() }
        val uiMessageManager = remember { UiMessageManager() }

        val seasons by observeShowSeasons.flow.collectAsState(emptyList())
        val show by observeShowDetails.flow.collectAsState(TiviShow.EMPTY_SHOW)
        val refreshing by loadingState.observable.collectAsState(false)
        val message by uiMessageManager.message.collectAsState(null)

        fun eventSink(event: ShowSeasonsUiEvent) {
            when (event) {
                is ShowSeasonsUiEvent.ClearMessage -> {
                    scope.launch {
                        uiMessageManager.clearMessage(event.id)
                    }
                }

                is ShowSeasonsUiEvent.Refresh -> {
                    scope.launch {
                        updateShowSeasons(
                            UpdateShowSeasons.Params(screen.id, event.fromUser),
                        ).collectStatus(loadingState, logger, uiMessageManager)
                    }
                }

                ShowSeasonsUiEvent.NavigateBack -> navigator.pop()
                is ShowSeasonsUiEvent.OpenEpisodeDetails -> {
                    navigator.goTo(EpisodeDetailsScreen(event.episodeId))
                }
            }
        }

        LaunchedEffect(Unit) {
            observeShowDetails(ObserveShowDetails.Params(screen.id))
            observeShowSeasons(ObserveShowSeasonsEpisodesWatches.Params(screen.id))

            eventSink(ShowSeasonsUiEvent.Refresh(false))
        }

        return ShowSeasonsUiState(
            show = show,
            seasons = seasons,
            refreshing = refreshing,
            message = message,
            initialSeasonId = screen.selectedSeasonId,
            eventSink = ::eventSink,
        )
    }
}
