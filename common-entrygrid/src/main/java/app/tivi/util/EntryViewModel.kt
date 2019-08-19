/*
 * Copyright 2017 Google LLC
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

package app.tivi.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import app.tivi.api.UiError
import app.tivi.api.UiIdle
import app.tivi.api.UiLoading
import app.tivi.api.UiSuccess
import app.tivi.api.UiStatus
import app.tivi.base.InvokeFinished
import app.tivi.base.InvokeStarted
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.domain.PagingInteractor
import app.tivi.base.InvokeStatus
import app.tivi.base.InvokeTimeout
import app.tivi.tmdb.TmdbManager
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.broadcastIn
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class EntryViewModel<LI : EntryWithShow<out Entry>, PI : PagingInteractor<*, LI>>(
    private val pageSize: Int = 21
) : ViewModel() {
    protected abstract val dispatchers: AppCoroutineDispatchers
    protected abstract val pagingInteractor: PI
    protected abstract val tmdbManager: TmdbManager
    protected abstract val logger: Logger

    private val messages = ConflatedBroadcastChannel<UiStatus>(UiIdle)
    private val loaded = ConflatedBroadcastChannel(false)

    protected val pageListConfig = PagedList.Config.Builder().run {
        setPageSize(pageSize * 3)
        setPrefetchDistance(pageSize)
        setEnablePlaceholders(false)
        build()
    }

    protected val boundaryCallback = object : PagedList.BoundaryCallback<LI>() {
        override fun onItemAtEndLoaded(itemAtEnd: LI) = onListScrolledToEnd()

        override fun onItemAtFrontLoaded(itemAtFront: LI) {
            loaded.offer(true)
        }

        override fun onZeroItemsLoaded() {
            loaded.offer(true)
        }
    }

    val viewState: Flow<EntryViewState<LI>> by lazy(LazyThreadSafetyMode.NONE) {
        combine(
                messages.asFlow(),
                tmdbManager.imageProviderFlow,
                pagingInteractor.observe().flowOn(pagingInteractor.dispatcher),
                loaded.asFlow()
        ) { message, imageProvider, pagedList, loaded ->
            EntryViewState(message, imageProvider, pagedList, loaded)
        }.broadcastIn(viewModelScope).asFlow()
    }

    fun onListScrolledToEnd() {
        callLoadMore().also {
            viewModelScope.launch {
                it.catch {
                    messages.send(UiError(it))
                }.map {
                    when (it) {
                        InvokeFinished -> UiSuccess
                        InvokeStarted -> UiLoading(false)
                        InvokeTimeout -> UiError()
                        else -> UiIdle
                    }
                }.collect {
                    messages.send(it)
                }
            }
        }
    }

    fun refresh() = refresh(true)

    protected fun refresh(fromUser: Boolean) {
        callRefresh(fromUser).also {
            viewModelScope.launch {
                it.catch {
                    messages.send(UiError(it))
                }.map {
                    when (it) {
                        InvokeFinished -> UiSuccess
                        InvokeStarted -> UiLoading(true)
                        InvokeTimeout -> UiError()
                        else -> UiIdle
                    }
                }.collect {
                    messages.send(it)
                }
            }
        }
    }

    protected abstract fun callRefresh(fromUser: Boolean): Flow<InvokeStatus>

    protected abstract fun callLoadMore(): Flow<InvokeStatus>
}