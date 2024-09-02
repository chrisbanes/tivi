// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.data.compoundmodels.AnticipatedShowEntryWithShow
import app.tivi.data.daos.AnticipatedShowsDao
import app.tivi.domain.PaginatedEntryRemoteMediator
import app.tivi.domain.PagingInteractor
import app.tivi.domain.interactors.UpdateAnticipatedShows
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObservePagedAnticipatedShows(
  private val popularDao: AnticipatedShowsDao,
  private val updateAnticipatedShows: UpdateAnticipatedShows,
) : PagingInteractor<ObservePagedAnticipatedShows.Params, AnticipatedShowEntryWithShow>() {
  @OptIn(androidx.paging.ExperimentalPagingApi::class)
  override fun createObservable(
    params: Params,
  ): Flow<PagingData<AnticipatedShowEntryWithShow>> {
    return Pager(
      config = params.pagingConfig,
      remoteMediator = PaginatedEntryRemoteMediator { page ->
        try {
          updateAnticipatedShows(UpdateAnticipatedShows.Params(page = page, isUserInitiated = true))
        } catch (ce: CancellationException) {
          throw ce
        } catch (t: Throwable) {
          Logger.e(t) { "Error while fetching from RemoteMediator" }
          throw t
        }
      },
      pagingSourceFactory = popularDao::entriesPagingSource,
    ).flow
  }

  data class Params(
    override val pagingConfig: PagingConfig,
  ) : Parameters<AnticipatedShowEntryWithShow>
}
