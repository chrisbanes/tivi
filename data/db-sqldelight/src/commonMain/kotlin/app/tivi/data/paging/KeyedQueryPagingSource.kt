/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.data.paging

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacter
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.TransacterBase
import app.cash.sqldelight.TransactionCallbacks
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext

internal class KeyedQueryPagingSource<Key : Any, RowType : Any>(
  private val queryProvider: (beginInclusive: Key, endExclusive: Key?) -> Query<RowType>,
  private val pageBoundariesProvider: (anchor: Key?, limit: Long) -> Query<Key>,
  private val transacter: TransacterBase,
  private val context: CoroutineContext,
) : QueryPagingSource<Key, RowType>() {

  private var pageBoundaries: List<Key>? = null
  override val jumpingSupported: Boolean get() = false

  override fun getRefreshKey(state: PagingState<Key, RowType>): Key? {
    val boundaries = pageBoundaries ?: return null
    val last = state.pages.lastOrNull() ?: return null
    val keyIndexFromNext = last.nextKey?.let { boundaries.indexOf(it) - 1 }
    val keyIndexFromPrev = last.prevKey?.let { boundaries.indexOf(it) + 1 }
    val keyIndex = keyIndexFromNext ?: keyIndexFromPrev ?: return null

    return boundaries.getOrNull(keyIndex)
  }

  override suspend fun load(params: LoadParams<Key>): LoadResult<Key, RowType> {
    return withContext(context) {
      try {
        val getPagingSourceLoadResult: TransactionCallbacks.() -> LoadResult<Key, RowType> = {
          val boundaries = pageBoundaries
            ?: pageBoundariesProvider(params.key, params.loadSize.toLong())
              .executeAsList()
              .also { pageBoundaries = it }

          val key = params.key ?: boundaries.first()

          require(key in boundaries)

          val keyIndex = boundaries.indexOf(key)
          val previousKey = boundaries.getOrNull(keyIndex - 1)
          val nextKey = boundaries.getOrNull(keyIndex + 1)
          val results = queryProvider(key, nextKey)
            .also { currentQuery = it }
            .executeAsList()

          LoadResult.Page(
            data = results,
            prevKey = previousKey,
            nextKey = nextKey,
          )
        }
        when (transacter) {
          is Transacter -> transacter.transactionWithResult(bodyWithReturn = getPagingSourceLoadResult)
          is SuspendingTransacter -> transacter.transactionWithResult(bodyWithReturn = getPagingSourceLoadResult)
        }
      } catch (e: Exception) {
        if (e is IllegalArgumentException) throw e
        LoadResult.Error<Key, RowType>(e)
      }
    }
  }
}
