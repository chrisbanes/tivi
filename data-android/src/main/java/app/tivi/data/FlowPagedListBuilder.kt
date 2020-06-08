/*
 * Copyright 2019 Google LLC
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

package app.tivi.data

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.paging.DataSource
import androidx.paging.PagedList
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

/**
 * Builder for `Flow<PagedList>` given a [DataSource.Factory] and a [PagedList.Config].
 *
 * The required parameters are in the constructor, so you can simply construct and build, or
 * optionally enable extra features (such as initial load key, or BoundaryCallback).
 *
 * The returned Flow will already be subscribed on the
 * [fetchExecutor], and will perform all loading on that scheduler. It will
 * already be observed on [notifyExecutor], and will dispatch new PagedLists,
 * as well as their updates to that scheduler.
 *
 * @param <K> Type of input valued used to load data from the DataSource. Must be integer if
 * you're using PositionalDataSource.
 * @param <V> Item type being presented.
 */
class FlowPagedListBuilder<K, V>(
    private val dataSourceFactory: DataSource.Factory<K, V>,
    private val config: PagedList.Config,

    /**
     * First loading key passed to the first PagedList/DataSource.
     * <p>
     * When a new PagedList/DataSource pair is created after the first, it acquires a load key from
     * the previous generation so that data is loaded around the position already being observed.
     *
     * @param key Initial load key passed to the first PagedList/DataSource.
     */
    var initialLoadKey: K? = null,

    /**
     * Typically used to load additional data from network when paging from local storage.
     *
     * Pass a BoundaryCallback to listen to when the PagedList runs out of data to load. If this
     * method is not called, or `null` is passed, you will not be notified when each
     * DataSource runs out of data to provide to its PagedList.
     *
     * If you are paging from a DataSource.Factory backed by local storage, you can set a
     * BoundaryCallback to know when there is no more information to page from local storage.
     * This is useful to page from the network when local storage is a cache of network data.
     *
     * Note that when using a BoundaryCallback with a `Flow<PagedList>`, method calls
     * on the callback may be dispatched multiple times - one for each PagedList/DataSource
     * pair. If loading network data from a BoundaryCallback, you should prevent multiple
     * dispatches of the same method from triggering multiple simultaneous network loads.
     */
    var boundaryCallback: PagedList.BoundaryCallback<*>? = null,

    private var notifyExecutor: Executor? = null,
    private var fetchExecutor: Executor? = null
) {
    /**
     * Constructs a `Flow<PagedList>`.
     *
     * @return The Flow of PagedLists
     */
    @SuppressLint("RestrictedApi")
    fun buildFlow(): Flow<PagedList<V>> = channelFlow {
        val nExecutor = notifyExecutor ?: ArchTaskExecutor.getMainThreadExecutor()
        val fExecutor = fetchExecutor ?: ArchTaskExecutor.getIOThreadExecutor()

        val nDispatcher = nExecutor.asCoroutineDispatcher()
        val fDispatcher = fExecutor.asCoroutineDispatcher()

        val invalidateCallback = object : ClearableInvalidatedCallback {
            private var prevList: PagedList<V>? = null
            private var dataSource: DataSource<K, V>? = null

            override fun onInvalidated() = sendNewList()

            override fun clear() {
                dataSource?.removeInvalidatedCallback(this)
            }

            private fun sendNewList() {
                launch(fDispatcher) {
                    // Compute on the fetch dispatcher
                    val list = createPagedList()

                    withContext(nDispatcher) {
                        // Send on the notify dispatcher
                        send(list)
                    }
                }
            }

            @Suppress("UNCHECKED_CAST")
            private fun createPagedList(): PagedList<V> {
                do {
                    dataSource?.removeInvalidatedCallback(this)

                    dataSource = dataSourceFactory.create().also {
                        it.addInvalidatedCallback(this)
                    }

                    val list = PagedList.Builder(dataSource!!, config)
                        .setNotifyExecutor(nExecutor)
                        .setFetchExecutor(fExecutor)
                        .setBoundaryCallback(boundaryCallback)
                        .setInitialKey(prevList?.lastKey as? K ?: initialLoadKey)
                        .build()
                        .also { prevList = it }
                } while (list.isDetached)

                return prevList!!
            }
        }

        // Do the initial load
        invalidateCallback.onInvalidated()

        awaitClose {
            invalidateCallback.clear()
        }
    }

    private interface ClearableInvalidatedCallback : DataSource.InvalidatedCallback {
        fun clear()
    }
}
