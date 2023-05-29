// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveShowSeasonsEpisodesWatches(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
) : SubjectInteractor<ObserveShowSeasonsEpisodesWatches.Params, List<SeasonWithEpisodesAndWatches>>() {

    override fun createObservable(params: Params): Flow<List<SeasonWithEpisodesAndWatches>> {
        return seasonsEpisodesRepository.observeSeasonsWithEpisodesWatchedForShow(params.showId)
    }

    data class Params(val showId: Long)
}
