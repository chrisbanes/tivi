/*
 * Copyright 2017 Google, Inc.
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

import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import app.tivi.api.Status
import app.tivi.api.UiResource
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.extensions.distinctUntilChanged
import app.tivi.extensions.toFlowable
import app.tivi.tmdb.TmdbManager
import io.reactivex.rxkotlin.Flowables
import io.reactivex.subjects.BehaviorSubject

abstract class EntryViewModel<LI : EntryWithShow<out Entry>>(
    private val schedulers: AppRxSchedulers,
    private val dispatchers: AppCoroutineDispatchers,
    private val dataSource: DataSource.Factory<Int, LI>,
    tmdbManager: TmdbManager,
    private val logger: Logger,
    private val pageSize: Int = 21
) : TiviViewModel() {
    private val messages = BehaviorSubject.create<UiResource>()

    val liveList by lazy(mode = LazyThreadSafetyMode.NONE) {
        LivePagedListBuilder<Int, LI>(
                dataSource,
                PagedList.Config.Builder().run {
                    setPageSize(pageSize * 3)
                    setPrefetchDistance(pageSize)
                    setEnablePlaceholders(false)
                    build()
                }
        ).setBoundaryCallback(object : PagedList.BoundaryCallback<LI>() {
            override fun onItemAtEndLoaded(itemAtEnd: LI) {
                onListScrolledToEnd()
            }
        }).build().distinctUntilChanged()
    }

    val viewState = LiveDataReactiveStreams.fromPublisher(
            Flowables.combineLatest(messages.toFlowable(), tmdbManager.imageProviderFlowable, ::EntryViewState)
    )

    init {
        refresh()
    }

    fun onListScrolledToEnd() {
        launchWithParent(dispatchers.main) {
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
        launchWithParent(dispatchers.main) {
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