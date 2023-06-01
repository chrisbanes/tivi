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
 * A [RemoteMediator] which works on [PaginatedEntry] entities. [fetch] will be called with the
 * next page to load.
 */
@Suppress("CAST_NEVER_SUCCEEDS", "USELESS_CAST", "KotlinRedundantDiagnosticSuppress")
@OptIn(app.cash.paging.ExperimentalPagingApi::class)
internal class PaginatedEntryRemoteMediator<LI, ET>(
    private val fetch: suspend (page: Int) -> Unit,
) : RemoteMediator<Int, LI>() where ET : PaginatedEntry, LI : EntryWithShow<ET> {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, LI>,
    ): RemoteMediatorMediatorResult {
        val nextPage = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return RemoteMediatorMediatorResultSuccess(endOfPaginationReached = true)
                as RemoteMediatorMediatorResult
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return RemoteMediatorMediatorResultSuccess(endOfPaginationReached = true)
                        as RemoteMediatorMediatorResult
                lastItem.entry.page + 1
            }
            else -> error("Unknown LoadType: $loadType")
        }
        return try {
            fetch(nextPage)
            RemoteMediatorMediatorResultSuccess(endOfPaginationReached = false)
                as RemoteMediatorMediatorResult
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            RemoteMediatorMediatorResultError(t) as RemoteMediatorMediatorResult
        }
    }
}
