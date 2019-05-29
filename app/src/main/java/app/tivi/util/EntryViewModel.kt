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

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import app.tivi.api.Status
import app.tivi.api.UiResource
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.extensions.toFlowable
import app.tivi.interactors.PagingInteractor
import app.tivi.tmdb.TmdbManager
import io.reactivex.rxkotlin.Flowables
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.launch

abstract class EntryViewModel<LI : EntryWithShow<out Entry>, PI : PagingInteractor<*, LI>>(
    private val dispatchers: AppCoroutineDispatchers,
    pagingInteractor: PI,
    tmdbManager: TmdbManager,
    private val logger: Logger,
    private val pageSize: Int = 21
) : TiviViewModel() {
    private val messages = BehaviorSubject.create<UiResource>()
    private val loaded = BehaviorSubject.createDefault(false)

    protected val pageListConfig = PagedList.Config.Builder().run {
        setPageSize(pageSize * 3)
        setPrefetchDistance(pageSize)
        setEnablePlaceholders(false)
        build()
    }

    val boundaryCallback = object : PagedList.BoundaryCallback<LI>() {
        override fun onItemAtEndLoaded(itemAtEnd: LI) = onListScrolledToEnd()
        override fun onItemAtFrontLoaded(itemAtFront: LI) = loaded.onNext(true)
        override fun onZeroItemsLoaded() = loaded.onNext(true)
    }

    val viewState = LiveDataReactiveStreams.fromPublisher(
            Flowables.combineLatest(
                    messages.toFlowable(),
                    tmdbManager.imageProviderFlowable,
                    pagingInteractor.observe().toFlowable(),
                    loaded.toFlowable(),
                    ::EntryViewState
            )
    )

    init {
        refresh()
    }

    fun onListScrolledToEnd() {
        viewModelScope.launch {
            sendMessage(UiResource(Status.LOADING_MORE))
            try {
                callLoadMore()
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
                callRefresh()
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    protected open suspend fun callRefresh() = Unit

    protected open suspend fun callLoadMore() = Unit

    private fun onError(t: Throwable) {
        logger.e(t)
        sendMessage(UiResource(Status.ERROR, t.localizedMessage))
    }

    private fun onSuccess() {
        sendMessage(UiResource(Status.SUCCESS))
    }

    private fun sendMessage(uiResource: UiResource) {
        messages.onNext(uiResource)
    }
}