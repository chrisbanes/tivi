/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.paging.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.CombinedLoadStates
import androidx.paging.ItemSnapshotList
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.paging.PagingSource
import androidx.paging.RemoteMediator
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext

/**
 * The class responsible for accessing the data from a [Flow] of [PagingData].
 * In order to obtain an instance of [LazyPagingItems] use the [collectAsLazyPagingItems] extension
 * method of [Flow] with [PagingData].
 * This instance can be used for Lazy foundations such as `LazyListScope.items` to display data
 * received from the [Flow] of [PagingData].
 *
 * Previewing [LazyPagingItems] is supported on a list of mock data. See sample for how to preview
 * mock data.
 *
 * @sample androidx.paging.compose.samples.PagingPreview
 *
 * @param T the type of value used by [PagingData].
 */
public class LazyPagingItems<T : Any> internal constructor(
  /**
   * the [Flow] object which contains a stream of [PagingData] elements.
   */
  private val flow: Flow<PagingData<T>>
) {
  private val mainDispatcher = Dispatchers.Main

  /**
   * If the [flow] is a SharedFlow, it is expected to be the flow returned by from
   * pager.flow.cachedIn(scope) which could contain a cached PagingData. We pass the cached
   * PagingData to the presenter so that if the PagingData contains cached data, the presenter
   * can be initialized with the data prior to collection on pager.
   */
  private val pagingDataPresenter = object : PagingDataPresenter<T>(
    mainContext = mainDispatcher,
    cachedPagingData =
    if (flow is SharedFlow<PagingData<T>>) flow.replayCache.firstOrNull() else null
  ) {
    override suspend fun presentPagingDataEvent(
      event: PagingDataEvent<T>,
    ) {
      updateItemSnapshotList()
    }
  }

  /**
   * Contains the immutable [ItemSnapshotList] of currently presented items, including any
   * placeholders if they are enabled.
   * Note that similarly to [peek] accessing the items in a list will not trigger any loads.
   * Use [get] to achieve such behavior.
   */
  var itemSnapshotList by mutableStateOf(
    pagingDataPresenter.snapshot()
  )
    private set

  /**
   * The number of items which can be accessed.
   */
  val itemCount: Int get() = itemSnapshotList.size

  private fun updateItemSnapshotList() {
    itemSnapshotList = pagingDataPresenter.snapshot()
  }

  /**
   * Returns the presented item at the specified position, notifying Paging of the item access to
   * trigger any loads necessary to fulfill prefetchDistance.
   *
   * @see peek
   */
  operator fun get(index: Int): T? {
    pagingDataPresenter[index] // this registers the value load
    return itemSnapshotList[index]
  }

  /**
   * Returns the presented item at the specified position, without notifying Paging of the item
   * access that would normally trigger page loads.
   *
   * @param index Index of the presented item to return, including placeholders.
   * @return The presented item at position [index], `null` if it is a placeholder
   */
  fun peek(index: Int): T? {
    return itemSnapshotList[index]
  }

  /**
   * Retry any failed load requests that would result in a [LoadState.Error] update to this
   * [LazyPagingItems].
   *
   * Unlike [refresh], this does not invalidate [PagingSource], it only retries failed loads
   * within the same generation of [PagingData].
   *
   * [LoadState.Error] can be generated from two types of load requests:
   *  * [PagingSource.load] returning [PagingSource.LoadResult.Error]
   *  * [RemoteMediator.load] returning [RemoteMediator.MediatorResult.Error]
   */
  fun retry() {
    pagingDataPresenter.retry()
  }

  /**
   * Refresh the data presented by this [LazyPagingItems].
   *
   * [refresh] triggers the creation of a new [PagingData] with a new instance of [PagingSource]
   * to represent an updated snapshot of the backing dataset. If a [RemoteMediator] is set,
   * calling [refresh] will also trigger a call to [RemoteMediator.load] with [LoadType] [REFRESH]
   * to allow [RemoteMediator] to check for updates to the dataset backing [PagingSource].
   *
   * Note: This API is intended for UI-driven refresh signals, such as swipe-to-refresh.
   * Invalidation due repository-layer signals, such as DB-updates, should instead use
   * [PagingSource.invalidate].
   *
   * @see PagingSource.invalidate
   */
  fun refresh() {
    pagingDataPresenter.refresh()
  }

  /**
   * A [CombinedLoadStates] object which represents the current loading state.
   */
  public var loadState: CombinedLoadStates by mutableStateOf(
    pagingDataPresenter.loadStateFlow.value
      ?: CombinedLoadStates(
        refresh = InitialLoadStates.refresh,
        prepend = InitialLoadStates.prepend,
        append = InitialLoadStates.append,
        source = InitialLoadStates
      )
  )
    private set

  internal suspend fun collectLoadState() {
    pagingDataPresenter.loadStateFlow.filterNotNull().collect {
      loadState = it
    }
  }

  internal suspend fun collectPagingData() {
    flow.collectLatest {
      pagingDataPresenter.collectFrom(it)
    }
  }
}

private val IncompleteLoadState = LoadState.NotLoading(false)
private val InitialLoadStates = LoadStates(
  LoadState.Loading,
  IncompleteLoadState,
  IncompleteLoadState
)

/**
 * Collects values from this [Flow] of [PagingData] and represents them inside a [LazyPagingItems]
 * instance. The [LazyPagingItems] instance can be used for lazy foundations such as
 * [LazyListScope.items] in order to display the data obtained from a [Flow] of [PagingData].
 *
 * @sample androidx.paging.compose.samples.PagingBackendSample
 *
 * @param context the [CoroutineContext] to perform the collection of [PagingData]
 * and [CombinedLoadStates].
 */
@Composable
public fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(
  context: CoroutineContext = EmptyCoroutineContext
): LazyPagingItems<T> {

  val lazyPagingItems = remember(this) { LazyPagingItems(this) }

  LaunchedEffect(lazyPagingItems) {
    if (context == EmptyCoroutineContext) {
      lazyPagingItems.collectPagingData()
    } else {
      withContext(context) {
        lazyPagingItems.collectPagingData()
      }
    }
  }

  LaunchedEffect(lazyPagingItems) {
    if (context == EmptyCoroutineContext) {
      lazyPagingItems.collectLoadState()
    } else {
      withContext(context) {
        lazyPagingItems.collectLoadState()
      }
    }
  }

  return lazyPagingItems
}
