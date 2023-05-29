// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.models.Episode
import app.tivi.domain.ResultInteractor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class GetEpisodeDetails(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val dispatchers: AppCoroutineDispatchers,
) : ResultInteractor<GetEpisodeDetails.Params, Episode?>() {
    override suspend fun doWork(params: Params): Episode? = withContext(dispatchers.io) {
        seasonsEpisodesRepository.getEpisode(params.episodeId)
    }

    data class Params(val episodeId: Long)
}
