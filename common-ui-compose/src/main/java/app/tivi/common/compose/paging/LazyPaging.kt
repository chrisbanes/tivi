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

package app.tivi.common.compose.paging

import androidx.collection.SparseArrayCompat
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.CombinedLoadStates
import androidx.paging.ItemSnapshotList
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingDataDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

/**
 * The class responsible for accessing the data from a [Flow] of [PagingData].
 * In order to obtain an instance of [LazyPagingItems] use the [collectAsLazyPagingItems] extension
 * method of [Flow] with [PagingData].
 * This instance can be used by the [items] and [itemsIndexed] methods inside [LazyListScope] to
 * display data received from the [Flow] of [PagingData].
 *
 * @param flow the [Flow] object which contains a stream of [PagingData] elements.
 * @param T the type of value used by [PagingData].
 */
class LazyPagingItems<T : Any> internal constructor(
    private val flow: Flow<PagingData<T>>,
    private val areItemsTheSame: (oldItem: T, newItem: T) -> Boolean,
    private val areContentsTheSame: (oldItem: T, newItem: T) -> Boolean,
) {
    /**
     * A sparse array of state objects used by the currently visible items. If we update the
     * value of the MutableState it will automatically trigger recomposition for this changed
     * item in the list (and not affect other items). This also helps us to track what indexes
     * are currently visible to ignore onChanged events for the rest of items.
     */
    private val currentlyUsedItems = SparseArrayCompat<MutableState<LazyListPagingItemState<T>>>(60)

    // This bakes [itemCount] property with a mutable state which means that every time we
    // update the state the usages of itemCount would be recomposed.
    private val _itemCount = mutableStateOf(0)

    /**
     * The number of items which can be accessed.
     */
    val itemCount: Int
        get() = _itemCount.value

    private val pagingDataDiffer = AsyncPagingDataDiffer(
        diffCallback = object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                return this@LazyPagingItems.areItemsTheSame(oldItem, newItem)
            }

            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                return this@LazyPagingItems.areContentsTheSame(oldItem, newItem)
            }
        },
        updateCallback = object : ListUpdateCallback {
            override fun onChanged(position: Int, count: Int, payload: Any?) {
                synchronized(currentlyUsedItems) {
                    for (index in position until position + count) {
                        // Mark all of the changed items as 'pending' so that the items are re-fetched.
                        // Ideally we would update the items now, but getItem() does not return
                        // the updated items until after the update.
                        currentlyUsedItems[index]?.run {
                            value.pending = true
                        }
                    }
                }
            }

            override fun onInserted(position: Int, count: Int) {
                _itemCount.value += count
            }

            override fun onRemoved(position: Int, count: Int) {
                _itemCount.value -= count
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                synchronized(currentlyUsedItems) {
                    val from = currentlyUsedItems.get(fromPosition)
                    currentlyUsedItems.remove(fromPosition)
                    currentlyUsedItems.put(toPosition, from)
                }
            }
        }
    )

    /**
     * Returns the item specified at [index] and notifies Paging of the item accessed in
     * order to trigger any loads necessary to fulfill [PagingConfig.prefetchDistance].
     *
     * @param index the index of the item which should be returned.
     * @return the item specified at [index] or null if the [index] is not between correct
     * bounds or the item is a placeholder.
     */
    @Composable
    operator fun get(index: Int): T? {
        val state = remember {
            mutableStateOf(LazyListPagingItemState(pagingDataDiffer.getItem(index)))
        }
        DisposableEffect(index) {
            synchronized(currentlyUsedItems) {
                currentlyUsedItems.put(index, state)
            }
            onDispose {
                synchronized(currentlyUsedItems) {
                    currentlyUsedItems.remove(index)
                }
            }
        }
        DisposableEffect(state.value.pending) {
            val itemState = state.value
            if (itemState.pending) {
                // If we're pending, re-fetch the item. We use peek() to not mess with
                // paging's logic for calculating when to load the next page
                itemState.item = pagingDataDiffer.peek(index)
                // Flip the pending flag back to false
                itemState.pending = false
            }
            onDispose { /* no-op */ }
        }
        return state.value.item
    }

    /**
     * Returns the presented item at the specified position, without notifying Paging of the item
     * access that would normally trigger page loads.
     *
     * @param index Index of the presented item to return, including placeholders.
     * @return The presented item at position [index], `null` if it is a placeholder
     */
    fun peek(index: Int): T? {
        return pagingDataDiffer.peek(index)
    }

    /**
     * Returns a new [ItemSnapshotList] representing the currently presented items, including any
     * placeholders if they are enabled.
     */
    fun snapshot(): ItemSnapshotList<T> {
        return pagingDataDiffer.snapshot()
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
        pagingDataDiffer.retry()
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
        pagingDataDiffer.refresh()
    }

    /**
     * A [CombinedLoadStates] object which represents the current loading state.
     */
    var loadState: CombinedLoadStates by mutableStateOf(
        CombinedLoadStates(
            refresh = InitialLoadStates.refresh,
            prepend = InitialLoadStates.prepend,
            append = InitialLoadStates.append,
            source = InitialLoadStates,
        )
    )
        private set

    internal suspend fun collectLoadState() {
        pagingDataDiffer.loadStateFlow.collect {
            loadState = it
        }
    }

    internal suspend fun collectPagingData() {
        flow.collectLatest {
            pagingDataDiffer.submitData(it)
        }
    }
}

private class LazyListPagingItemState<T>(item: T?) {
    var item: T? by mutableStateOf(item)
    var pending: Boolean by mutableStateOf(false)
}

private val IncompleteLoadState = LoadState.NotLoading(false)
private val InitialLoadStates = LoadStates(
    IncompleteLoadState,
    IncompleteLoadState,
    IncompleteLoadState
)

/**
 * Collects values from this [Flow] of [PagingData] and represents them inside a [LazyPagingItems]
 * instance. The [LazyPagingItems] instance can be used by the [items] and [itemsIndexed] methods
 * from [LazyListScope] in order to display the data obtained from a [Flow] of [PagingData].
 *
 * @sample androidx.paging.compose.samples.PagingBackendSample
 */
@Composable
fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(
    areContentsTheSame: (old: T, new: T) -> Boolean = { old, new -> old == new },
    areItemsTheSame: (old: T, new: T) -> Boolean
): LazyPagingItems<T> {
    val lazyPagingItems = remember(this) {
        LazyPagingItems(this, areItemsTheSame, areContentsTheSame)
    }

    LaunchedEffect(lazyPagingItems) {
        lazyPagingItems.collectPagingData()
    }

    LaunchedEffect(lazyPagingItems) {
        lazyPagingItems.collectLoadState()
    }

    return lazyPagingItems
}

/**
 * Adds the [LazyPagingItems] and their content to the scope. The range from 0 (inclusive) to
 * [LazyPagingItems.itemCount] (inclusive) always represents the full range of presentable items,
 * because every event from [PagingDataDiffer] will trigger a recomposition.
 *
 * @sample androidx.paging.compose.samples.ItemsDemo
 *
 * @param lazyPagingItems the items received from a [Flow] of [PagingData].
 * @param itemContent the content displayed by a single item. In case the item is `null`, the
 * [itemContent] method should handle the logic of displaying a placeholder instead of the main
 * content displayed by an item which is not `null`.
 */
fun <T : Any> LazyListScope.items(
    lazyPagingItems: LazyPagingItems<T>,
    itemContent: @Composable LazyItemScope.(value: T?) -> Unit
) {
    items(lazyPagingItems.itemCount) { index ->
        val item = lazyPagingItems[index]
        itemContent(item)
    }
}

/**
 * Adds the [LazyPagingItems] and their content to the scope where the content of an item is
 * aware of its local index. The range from 0 (inclusive) to [LazyPagingItems.itemCount] (inclusive)
 * always represents the full range of presentable items, because every event from
 * [PagingDataDiffer] will trigger a recomposition.
 *
 * @param lazyPagingItems the items received from a [Flow] of [PagingData].
 * @param itemContent the content displayed by a single item. In case the item is `null`, the
 * [itemContent] method should handle the logic of displaying a placeholder instead of the main
 * content displayed by an item which is not `null`.
 */
fun <T : Any> LazyListScope.itemsIndexed(
    lazyPagingItems: LazyPagingItems<T>,
    itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit
) {
    items(lazyPagingItems.itemCount) { index ->
        itemContent(index, lazyPagingItems[index])
    }
}
