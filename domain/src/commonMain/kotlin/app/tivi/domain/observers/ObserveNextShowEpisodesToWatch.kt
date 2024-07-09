// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.daos.WatchedShowDao
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveNextShowEpisodesToWatch(
  private val watchedShowDao: WatchedShowDao,
) : SubjectInteractor<ObserveNextShowEpisodesToWatch.Params, List<UpNextEntry>>() {
  override fun createObservable(params: Params): Flow<List<UpNextEntry>> {
    return watchedShowDao.observeUpNextShows(params.followedOnly, params.limit)
  }

  data class Params(val followedOnly: Boolean = false, val limit: Long = Long.MAX_VALUE)
}
