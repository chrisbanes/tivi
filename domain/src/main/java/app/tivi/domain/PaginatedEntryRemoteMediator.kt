/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.domain

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import app.tivi.data.PaginatedEntry
import app.tivi.data.resultentities.EntryWithShow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A [RemoteMediator] which works on [PaginatedEntry] entities. [fetch] will be called with the
 * next page to load.
 */
@OptIn(ExperimentalPagingApi::class)
internal class PaginatedEntryRemoteMediator<LI, ET>(
    private val fetchScope: CoroutineScope,
    private val fetch: suspend (page: Int) -> Unit
) : RemoteMediator<Int, LI>() where ET : PaginatedEntry, LI : EntryWithShow<ET> {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, LI>
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
            // Ideally we would not need to launch on a separate scope, but Paging 3.0.0-alpha07
            // has an issue where APPENDs are cancelled as after new items are inserted into
            // the DB. Our fetchers tend to: 1) fetch list items, 2) update the DB,
            // 3) fetch additional data. In alpha07, step 3 never happens because paging
            // cancels us after step #2. https://issuetracker.google.com/162252536
            // We can't update to alpha08+ because of other issues (see dependencies.kt), so
            // we workaround by launching on a separate scope.
            // TODO: remove this hack when we can
            fetchScope.launch {
                fetch(nextPage)
            }
            MediatorResult.Success(endOfPaginationReached = false)
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }
}
