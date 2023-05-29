// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveEpisodeWatches(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
) : SubjectInteractor<ObserveEpisodeWatches.Params, List<EpisodeWatchEntry>>() {
    override fun createObservable(params: Params): Flow<List<EpisodeWatchEntry>> {
        return seasonsEpisodesRepository.observeEpisodeWatches(params.episodeId)
    }

    data class Params(val episodeId: Long)
}
