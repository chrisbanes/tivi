// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.RelatedShowEntryWithShow
import app.tivi.data.daos.RelatedShowsDao
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveRelatedShows(
    private val relatedShowsDao: RelatedShowsDao,
) : SubjectInteractor<ObserveRelatedShows.Params, List<RelatedShowEntryWithShow>>() {

    override fun createObservable(params: Params): Flow<List<RelatedShowEntryWithShow>> {
        return relatedShowsDao.entriesWithShowsObservable(params.showId)
    }

    data class Params(val showId: Long)
}
