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

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedTask
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.CombinedLoadStates
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
    private val flow: Flow<PagingData<T>>
) {

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
            override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem === newItem
            override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
        },
        updateCallback = object : ListUpdateCallback {
            override fun onChanged(position: Int, count: Int, payload: Any?) {
                for (index in position until position + count) {
                    val state = currentlyUsedItems[position]
                    if (state != null) {
                        state.value.pending = true
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
                // TODO
            }
        }
    )

    /**
     * A map of state objects used by the currently visible items. If we update the value of the
     * MutableState it will automatically trigger recomposition for this changed item in the list
     * (and not affect other items). This also helps us to track what indexes are currently
     * visible to ignore onChanged events for the rest of items.
     */
    private val currentlyUsedItems = mutableMapOf<Int, MutableState<LazyListPagingItemState<T>>>()

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
        onCommit(index) {
            currentlyUsedItems[index] = state
            onDispose {
                currentlyUsedItems.remove(index)
            }
        }
        onCommit(state.value.pending) {
            if (state.value.pending) {
                state.value.item = pagingDataDiffer.getItem(index)
                state.value.pending = false
            }
        }
        return state.value.item
    }

    /**
     * A [CombinedLoadStates] object which represents the current loading state.
     */
    var loadState: CombinedLoadStates by mutableStateOf(CombinedLoadStates(InitialLoadStates))
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
    var item: T? by mutableStateOf(null)
    var pending: Boolean by mutableStateOf(false)

    init {
        this.item = item
    }
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
fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(): LazyPagingItems<T> {
    val lazyPagingItems = remember(this) { LazyPagingItems(this) }

    LaunchedTask(lazyPagingItems) {
        lazyPagingItems.collectPagingData()
    }

    LaunchedTask(lazyPagingItems) {
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
    items((0 until lazyPagingItems.itemCount).toList()) { index ->
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
 * @sample androidx.paging.compose.samples.ItemsIndexedDemo
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
    items((0 until lazyPagingItems.itemCount).toList()) { index ->
        val item = lazyPagingItems[index]
        itemContent(index, item)
    }
}
