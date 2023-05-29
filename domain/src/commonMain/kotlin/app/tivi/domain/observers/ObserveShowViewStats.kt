// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.views.ShowsWatchStats
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveShowViewStats(
    private val dao: WatchedShowDao,
) : SubjectInteractor<ObserveShowViewStats.Params, ShowsWatchStats?>() {

    override fun createObservable(params: Params): Flow<ShowsWatchStats?> {
        return dao.entryShowViewStats(params.showId)
    }

    data class Params(val showId: Long)
}
