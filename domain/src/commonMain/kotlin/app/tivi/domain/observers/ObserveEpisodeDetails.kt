// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.EpisodeWithSeason
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveEpisodeDetails(
  private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
) : SubjectInteractor<ObserveEpisodeDetails.Params, EpisodeWithSeason>() {

  override fun createObservable(params: Params): Flow<EpisodeWithSeason> {
    return seasonsEpisodesRepository.observeEpisode(params.episodeId)
  }

  data class Params(val episodeId: Long)
}
