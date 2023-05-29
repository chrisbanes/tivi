// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain

import androidx.paging.ExperimentalPagingApi
import app.cash.paging.LoadType
import app.cash.paging.PagingState
import app.cash.paging.RemoteMediator
import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.models.PaginatedEntry
import kotlinx.coroutines.CancellationException

/**
 * A [RemoteMediator] which works on [PaginatedEntry] entities, but only calls
 * [fetch] for [LoadType.REFRESH] events.
 */
@OptIn(ExperimentalPagingApi::class)
internal class RefreshOnlyRemoteMediator<LI, ET>(
    private val fetch: suspend () -> Unit,
) : RemoteMediator<Int, LI>() where ET : PaginatedEntry, LI : EntryWithShow<ET> {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, LI>,
    ): MediatorResult {
        if (loadType == LoadType.PREPEND || loadType == LoadType.APPEND) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        return try {
            fetch()
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }
}
