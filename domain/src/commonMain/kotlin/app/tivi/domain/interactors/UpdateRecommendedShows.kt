// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.recommendedshows.RecommendedShowsStore
import app.tivi.data.shows.ShowStore
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.data.util.fetch
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdateRecommendedShows.Params
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import app.tivi.util.parallelForEach
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateRecommendedShows(
    private val recommendedShowsStore: RecommendedShowsStore,
    private val showStore: ShowStore,
    private val dispatchers: AppCoroutineDispatchers,
    private val traktAuthRepository: TraktAuthRepository,
    private val logger: Logger,
) : Interactor<Params, Unit>() {
    override suspend fun doWork(params: Params) {
        // If we're not logged in, we can't load the recommended shows
        if (traktAuthRepository.state.value != TraktAuthState.LOGGED_IN) return

        withContext(dispatchers.io) {
            recommendedShowsStore.fetch(0, forceFresh = params.forceRefresh).parallelForEach {
                try {
                    showStore.fetch(it.showId)
                } catch (ce: CancellationException) {
                    throw ce
                } catch (t: Throwable) {
                    logger.e(t, "Error while show info: ${it.showId}")
                }
            }
        }
    }

    data class Params(val forceRefresh: Boolean = false)
}
