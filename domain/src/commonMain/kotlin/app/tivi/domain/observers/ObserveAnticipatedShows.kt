// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.AnticipatedShowEntryWithShow
import app.tivi.data.daos.AnticipatedShowsDao
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveAnticipatedShows(
  private val dao: AnticipatedShowsDao,
) : SubjectInteractor<ObserveAnticipatedShows.Params, List<AnticipatedShowEntryWithShow>>() {

  override fun createObservable(
    params: Params,
  ): Flow<List<AnticipatedShowEntryWithShow>> {
    return dao.entriesObservable(params.count, 0)
  }

  data class Params(val count: Int = 20)
}
