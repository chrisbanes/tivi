// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.daos.WatchedShowDao
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveNextShowEpisodeToWatch(
  private val watchedShowDao: WatchedShowDao,
) : SubjectInteractor<Unit, UpNextEntry?>() {
  override fun createObservable(params: Unit): Flow<UpNextEntry?> {
    return watchedShowDao.observeUpNextShow()
  }
}
