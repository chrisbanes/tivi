// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.relatedshows.RelatedShowsStore
import app.tivi.data.shows.ShowStore
import app.tivi.data.util.fetch
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdateRelatedShows.Params
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import app.tivi.util.parallelForEach
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateRelatedShows(
    private val relatedShowsStore: RelatedShowsStore,
    private val showsStore: ShowStore,
    private val dispatchers: AppCoroutineDispatchers,
    private val logger: Logger,
) : Interactor<Params, Unit>() {
    override suspend fun doWork(params: Params) = withContext(dispatchers.io) {
        relatedShowsStore.fetch(params.showId, params.forceLoad).related.parallelForEach {
            try {
                showsStore.fetch(it.otherShowId)
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                logger.e("Error while show info: ${it.otherShowId}")
            }
        }
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}
