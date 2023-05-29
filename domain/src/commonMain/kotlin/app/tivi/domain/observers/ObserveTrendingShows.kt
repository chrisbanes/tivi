// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.TrendingEntryWithShow
import app.tivi.data.daos.TrendingDao
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveTrendingShows(
    private val trendingShowsDao: TrendingDao,
) : SubjectInteractor<ObserveTrendingShows.Params, List<TrendingEntryWithShow>>() {

    override fun createObservable(params: Params): Flow<List<TrendingEntryWithShow>> {
        return trendingShowsDao.entriesObservable(params.count, 0)
    }

    data class Params(val count: Int = 20)
}
