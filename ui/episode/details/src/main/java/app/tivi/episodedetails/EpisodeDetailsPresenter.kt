// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.episodedetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.tivi.api.UiMessage
import app.tivi.api.UiMessageManager
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.domain.interactors.RemoveEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatches
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeWatches
import app.tivi.screens.EpisodeDetailsScreen
import app.tivi.screens.EpisodeTrackScreen
import app.tivi.util.Logger
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class EpisodeDetailsUiPresenterFactory(
    private val presenterFactory: (EpisodeDetailsScreen, Navigator) -> EpisodeDetailsPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? = when (screen) {
        is EpisodeDetailsScreen -> presenterFactory(screen, navigator)
        else -> null
    }
}

@Inject
class EpisodeDetailsPresenter(
    @Assisted private val screen: EpisodeDetailsScreen,
    @Assisted private val navigator: Navigator,
    private val updateEpisodeDetails: UpdateEpisodeDetails,
    private val observeEpisodeDetails: ObserveEpisodeDetails,
    private val observeEpisodeWatches: ObserveEpisodeWatches,
    private val removeEpisodeWatches: RemoveEpisodeWatches,
    private val removeEpisodeWatch: RemoveEpisodeWatch,
    private val logger: Logger,
) : Presenter<EpisodeDetailsUiState> {
    @Composable
    override fun present(): EpisodeDetailsUiState {
        val scope = rememberCoroutineScope()
        val uiMessageManager = remember { UiMessageManager() }

        val refreshing by updateEpisodeDetails.inProgress.collectAsState(false)
        val message by uiMessageManager.message.collectAsState(null)

        val episodeDetails by observeEpisodeDetails.flow.collectAsState(null)
        val episodeWatches by observeEpisodeWatches.flow.collectAsState(emptyList())

        fun eventSink(event: EpisodeDetailsUiEvent) {
            when (event) {
                is EpisodeDetailsUiEvent.Refresh -> {
                    scope.launch {
                        updateEpisodeDetails(
                            UpdateEpisodeDetails.Params(screen.id, event.fromUser),
                        ).also { result ->
                            result.exceptionOrNull()?.let { e ->
                                logger.i(e)
                                uiMessageManager.emitMessage(UiMessage(e))
                            }
                        }
                    }
                }

                is EpisodeDetailsUiEvent.ClearMessage -> {
                    scope.launch {
                        uiMessageManager.clearMessage(event.id)
                    }
                }

                EpisodeDetailsUiEvent.RemoveAllWatches -> {
                    scope.launch {
                        removeEpisodeWatches(
                            RemoveEpisodeWatches.Params(screen.id),
                        ).also { result ->
                            result.exceptionOrNull()?.let { e ->
                                logger.i(e)
                                uiMessageManager.emitMessage(UiMessage(e))
                            }
                        }
                    }
                }

                is EpisodeDetailsUiEvent.RemoveWatchEntry -> {
                    scope.launch {
                        removeEpisodeWatch(
                            RemoveEpisodeWatch.Params(event.id),
                        ).also { result ->
                            result.exceptionOrNull()?.let { e ->
                                logger.i(e)
                                uiMessageManager.emitMessage(UiMessage(e))
                            }
                        }
                    }
                }

                EpisodeDetailsUiEvent.NavigateUp -> navigator.pop()
                EpisodeDetailsUiEvent.OpenTrackEpisode -> {
                    navigator.goTo(EpisodeTrackScreen(screen.id))
                }
            }
        }

        LaunchedEffect(Unit) {
            observeEpisodeDetails(ObserveEpisodeDetails.Params(screen.id))
            observeEpisodeWatches(ObserveEpisodeWatches.Params(screen.id))

            eventSink(EpisodeDetailsUiEvent.Refresh(fromUser = false))
        }

        return EpisodeDetailsUiState(
            episode = episodeDetails?.episode,
            season = episodeDetails?.season,
            watches = episodeWatches,
            canAddEpisodeWatch = episodeDetails?.episode?.hasAired ?: false,
            refreshing = refreshing,
            message = message,
            eventSink = ::eventSink,
        )
    }
}
