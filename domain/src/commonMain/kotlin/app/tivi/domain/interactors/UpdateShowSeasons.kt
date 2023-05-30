// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdateShowSeasons.Params
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateShowSeasons(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<Params, Unit>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            // Then update the seasons/episodes
            if (params.forceRefresh || seasonsEpisodesRepository.needShowSeasonsUpdate(params.showId)) {
                seasonsEpisodesRepository.updateSeasonsEpisodes(params.showId)
            }

            ensureActive()
            // Finally update any watched progress
            if (params.forceRefresh || seasonsEpisodesRepository.needShowEpisodeWatchesSync(params.showId)) {
                seasonsEpisodesRepository.syncEpisodeWatchesForShow(params.showId)
            }
        }
    }

    data class Params(val showId: Long, val forceRefresh: Boolean)
}
