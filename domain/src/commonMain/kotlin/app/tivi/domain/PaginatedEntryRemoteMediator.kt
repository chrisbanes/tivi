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
 * A [RemoteMediator] which works on [PaginatedEntry] entities. [fetch] will be called with the
 * next page to load.
 */

@OptIn(ExperimentalPagingApi::class)
internal class PaginatedEntryRemoteMediator<LI, ET>(
    private val fetch: suspend (page: Int) -> Unit,
) : RemoteMediator<Int, LI>() where ET : PaginatedEntry, LI : EntryWithShow<ET> {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, LI>,
    ): MediatorResult {
        val nextPage = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                lastItem.entry.page + 1
            }
        }
        return try {
            fetch(nextPage)
            MediatorResult.Success(endOfPaginationReached = false)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }
}
