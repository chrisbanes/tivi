/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.episodedetails

import android.app.Activity
import androidx.compose.runtime.Stable
import app.tivi.api.UiError
import app.tivi.base.InvokeError
import app.tivi.base.InvokeStarted
import app.tivi.base.InvokeStatus
import app.tivi.base.InvokeSuccess
import app.tivi.domain.interactors.AddEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatch
import app.tivi.domain.interactors.RemoveEpisodeWatches
import app.tivi.domain.interactors.UpdateEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeDetails
import app.tivi.domain.observers.ObserveEpisodeWatches
import app.tivi.ui.SnackbarManager
import app.tivi.util.Logger
import app.tivi.util.ObservableLoadingCounter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime

/**
 * Creates an injected [EpisodeDetailsViewModel] using Hilt.
 */
fun createEpisodeDetailsViewModel(
    episodeId: Long,
    activity: Activity,
    coroutineScope: CoroutineScope,
): EpisodeDetailsViewModel {
    // Use Hilt EntryPointAccessors to create an injected assisted factory,
    // then create a ViewModel with the given episodeId. This is remember-ed using the
    // id as the key, so it will be 'cleared' if the ID changes
    return EntryPointAccessors.fromActivity(
        activity,
        EpisodeDetailsViewModelEntryPoint::class.java
    ).episodeDetailsViewModelFactory().create(
        episodeId = episodeId,
        coroutineScope = coroutineScope,
    )
}

/**
 * A [EntryPoint] which allows us to inject using Hilt on-demand.
 *
 * See https://developer.android.com/training/dependency-injection/hilt-android#not-supported
 */
@EntryPoint
@InstallIn(ActivityComponent::class)
internal interface EpisodeDetailsViewModelEntryPoint {
    fun episodeDetailsViewModelFactory(): EpisodeDetailsViewModelFactory
}

/**
 * Our Hilt [AssistedFactory] which allows us to inject a [EpisodeDetailsViewModel], but
 * also pass in the `episodeId` and `coroutineScope`.
 */
@AssistedFactory
internal interface EpisodeDetailsViewModelFactory {
    fun create(
        episodeId: Long,
        coroutineScope: CoroutineScope
    ): EpisodeDetailsViewModel
}

@Stable
class EpisodeDetailsViewModel @AssistedInject constructor(
    @Assisted private val coroutineScope: CoroutineScope,
    @Assisted private val episodeId: Long,
    private val updateEpisodeDetails: UpdateEpisodeDetails,
    observeEpisodeDetails: ObserveEpisodeDetails,
    observeEpisodeWatches: ObserveEpisodeWatches,
    private val addEpisodeWatch: AddEpisodeWatch,
    private val removeEpisodeWatches: RemoveEpisodeWatches,
    private val removeEpisodeWatch: RemoveEpisodeWatch,
    private val logger: Logger,
    private val snackbarManager: SnackbarManager,
) {
    private val loadingState = ObservableLoadingCounter()

    private val pendingActions = MutableSharedFlow<EpisodeDetailsAction>()

    /**
     * Our main 'ui state' flow. We use stateIn() to make it a hot flow which remains active
     * for 5 seconds after all collectors are removed. This useful for when the app goes into
     * the background as our composables use flowWithLifecycle().
     *
     * See https://medium.com/androiddevelopers/migrating-from-livedata-to-kotlins-flow-379292f419fb
     */
    val state: StateFlow<EpisodeDetailsViewState> = combine(
        observeEpisodeDetails.flow,
        observeEpisodeWatches.flow,
        loadingState.observable,
        snackbarManager.errors,
    ) { episodeDetails, episodeWatches, refreshing, error ->
        EpisodeDetailsViewState(
            episode = episodeDetails.episode,
            season = episodeDetails.season,
            watches = episodeWatches,
            canAddEpisodeWatch = episodeDetails.episode?.firstAired?.isBefore(OffsetDateTime.now())
                ?: true,
            refreshing = refreshing,
            error = error,
        )
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EpisodeDetailsViewState.Empty,
    )

    init {
        coroutineScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    EpisodeDetailsAction.RefreshAction -> refresh(true)
                    EpisodeDetailsAction.AddEpisodeWatchAction -> markWatched()
                    EpisodeDetailsAction.RemoveAllEpisodeWatchesAction -> markUnwatched()
                    is EpisodeDetailsAction.RemoveEpisodeWatchAction -> removeWatchEntry(action)
                    EpisodeDetailsAction.ClearError -> snackbarManager.removeCurrentError()
                }
            }
        }

        observeEpisodeDetails(ObserveEpisodeDetails.Params(episodeId))
        observeEpisodeWatches(ObserveEpisodeWatches.Params(episodeId))

        refresh(false)
    }

    internal fun submitAction(action: EpisodeDetailsAction) {
        coroutineScope.launch {
            pendingActions.emit(action)
        }
    }

    private fun refresh(fromUserInteraction: Boolean) {
        updateEpisodeDetails(
            UpdateEpisodeDetails.Params(episodeId, fromUserInteraction)
        ).watchStatus()
    }

    private fun removeWatchEntry(action: EpisodeDetailsAction.RemoveEpisodeWatchAction) {
        removeEpisodeWatch(RemoveEpisodeWatch.Params(action.watchId)).watchStatus()
    }

    private fun markWatched() {
        addEpisodeWatch(AddEpisodeWatch.Params(episodeId, OffsetDateTime.now())).watchStatus()
    }

    private fun markUnwatched() {
        removeEpisodeWatches(RemoveEpisodeWatches.Params(episodeId)).watchStatus()
    }

    private fun Flow<InvokeStatus>.watchStatus() = coroutineScope.launch { collectStatus() }

    private suspend fun Flow<InvokeStatus>.collectStatus() = collect { status ->
        when (status) {
            InvokeStarted -> loadingState.addLoader()
            InvokeSuccess -> loadingState.removeLoader()
            is InvokeError -> {
                logger.i(status.throwable)
                snackbarManager.addError(UiError(status.throwable))
                loadingState.removeLoader()
            }
        }
    }
}
