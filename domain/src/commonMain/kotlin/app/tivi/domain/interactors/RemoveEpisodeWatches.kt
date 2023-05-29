// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class RemoveEpisodeWatches(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<RemoveEpisodeWatches.Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            seasonsEpisodesRepository.removeAllEpisodeWatches(params.episodeId)
        }
    }

    data class Params(val episodeId: Long)
}
