// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain

import app.cash.paging.LoadType
import app.cash.paging.PagingState
import app.cash.paging.RemoteMediator
import app.cash.paging.RemoteMediatorMediatorResult
import app.cash.paging.RemoteMediatorMediatorResultError
import app.cash.paging.RemoteMediatorMediatorResultSuccess
import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.models.PaginatedEntry
import kotlinx.coroutines.CancellationException

/**
 * A [RemoteMediator] which works on [PaginatedEntry] entities, but only calls
 * [fetch] for [LoadType.REFRESH] events.
 */
@Suppress("CAST_NEVER_SUCCEEDS", "USELESS_CAST", "KotlinRedundantDiagnosticSuppress")
@OptIn(app.cash.paging.ExperimentalPagingApi::class)
internal class RefreshOnlyRemoteMediator<LI, ET>(
  private val fetch: suspend () -> Unit,
) : RemoteMediator<Int, LI>() where ET : PaginatedEntry, LI : EntryWithShow<ET> {

  override suspend fun load(
    loadType: LoadType,
    state: PagingState<Int, LI>,
  ): RemoteMediatorMediatorResult {
    if (loadType == LoadType.PREPEND || loadType == LoadType.APPEND) {
      return RemoteMediatorMediatorResultSuccess(endOfPaginationReached = true)
        as RemoteMediatorMediatorResult
    }
    return try {
      fetch()
      RemoteMediatorMediatorResultSuccess(endOfPaginationReached = true)
        as RemoteMediatorMediatorResult
    } catch (ce: CancellationException) {
      throw ce
    } catch (t: Throwable) {
      RemoteMediatorMediatorResultError(t) as RemoteMediatorMediatorResult
    }
  }
}
