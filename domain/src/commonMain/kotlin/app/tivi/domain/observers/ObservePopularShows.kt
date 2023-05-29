// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.PopularEntryWithShow
import app.tivi.data.daos.PopularDao
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObservePopularShows(
    private val popularShowsRepository: PopularDao,
) : SubjectInteractor<ObservePopularShows.Params, List<PopularEntryWithShow>>() {

    override fun createObservable(
        params: Params,
    ): Flow<List<PopularEntryWithShow>> {
        return popularShowsRepository.entriesObservable(params.count, 0)
    }

    data class Params(val count: Int = 20)
}
