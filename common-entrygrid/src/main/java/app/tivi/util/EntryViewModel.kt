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
import app.tivi.api.Status
import app.tivi.api.UiResource
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.domain.PagingInteractor
import app.tivi.tmdb.TmdbManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

abstract class EntryViewModel<LI : EntryWithShow<out Entry>, PI : PagingInteractor<*, LI>>(
    private val dispatchers: AppCoroutineDispatchers,
    pagingInteractor: PI,
    tmdbManager: TmdbManager,
    private val logger: Logger,
    private val pageSize: Int = 21
) : ViewModel() {
    private val messages = ConflatedBroadcastChannel<UiResource>()
    private val loaded = ConflatedBroadcastChannel(false)

    protected val pageListConfig = PagedList.Config.Builder().run {
        setPageSize(pageSize * 3)
        setPrefetchDistance(pageSize)
        setEnablePlaceholders(false)
        build()
    }

    val boundaryCallback = object : PagedList.BoundaryCallback<LI>() {
        override fun onItemAtEndLoaded(itemAtEnd: LI) = onListScrolledToEnd()

        override fun onItemAtFrontLoaded(itemAtFront: LI) {
            viewModelScope.launch {
                loaded.offer(true)
            }
        }

        override fun onZeroItemsLoaded() {
            viewModelScope.launch {
                loaded.offer(true)
            }
        }
    }

    val viewState = combine(
            messages.asFlow(),
            tmdbManager.imageProviderFlow,
            pagingInteractor.observe().flowOn(pagingInteractor.dispatcher),
            loaded.asFlow()
    ) { message, imageProvider, pagedList, loaded ->
        EntryViewState(message, imageProvider, pagedList, loaded)
    }

    init {
        refresh()
    }

    fun onListScrolledToEnd() {
        viewModelScope.launch {
            sendMessage(UiResource(Status.LOADING_MORE))
            try {
                callLoadMore().join()
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            sendMessage(UiResource(Status.REFRESHING))
            try {
                callRefresh().join()
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    protected abstract suspend fun callRefresh(): Job

    protected abstract suspend fun callLoadMore(): Job

    private fun onError(t: Throwable) {
        logger.e(t)
        viewModelScope.launch {
            sendMessage(UiResource(Status.ERROR, t.localizedMessage))
        }
    }

    private fun onSuccess() {
        viewModelScope.launch {
            sendMessage(UiResource(Status.SUCCESS))
        }
    }

    private suspend fun sendMessage(uiResource: UiResource) = messages.offer(uiResource)
}