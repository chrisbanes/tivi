// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.RecommendedEntryWithShow
import app.tivi.data.daos.RecommendedDao
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveRecommendedShows(
    private val recommendedDao: RecommendedDao,
) : SubjectInteractor<ObserveRecommendedShows.Params, List<RecommendedEntryWithShow>>() {

    override fun createObservable(
        params: Params,
    ): Flow<List<RecommendedEntryWithShow>> {
        return recommendedDao.entriesObservable(params.count, 0)
    }

    data class Params(val count: Int = 20)
}
