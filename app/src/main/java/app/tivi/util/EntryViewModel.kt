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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import io.reactivex.BackpressureStrategy
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import app.tivi.api.Status
import app.tivi.api.UiResource
import app.tivi.calls.ListCall
import app.tivi.calls.PaginatedCall
import app.tivi.data.Entry
import app.tivi.data.entities.ListItem
import app.tivi.tmdb.TmdbManager

open class EntryViewModel<LI : ListItem<out Entry>>(
    private val schedulers: AppRxSchedulers,
    private val dispatchers: AppCoroutineDispatchers,
    private val call: ListCall<Unit, LI>,
    tmdbManager: TmdbManager,
    private val networkDetector: NetworkDetector,
    private val logger: Logger
) : TiviViewModel() {

    private val messages = BehaviorSubject.create<UiResource>()

    val liveList by lazy(mode = LazyThreadSafetyMode.NONE) {
        LivePagedListBuilder<Int, LI>(
                call.dataSourceFactory(),
                PagedList.Config.Builder().run {
                    setPageSize(call.pageSize * 3)
                    setPrefetchDistance(call.pageSize)
                    setEnablePlaceholders(false)
                    build()
                }
        ).run {
            build()
        }
    }

    val viewState: LiveData<EntryViewState> = LiveDataReactiveStreams.fromPublisher(
            Flowables.combineLatest(
                    messages.toFlowable(BackpressureStrategy.LATEST),
                    tmdbManager.imageProvider,
                    ::EntryViewState)
    )

    init {
        refresh()
    }

    fun onListScrolledToEnd() {
        if (call is PaginatedCall<*, *>) {
            launchWithParent(dispatchers.main) {
                sendMessage(UiResource(Status.LOADING_MORE))
                try {
                    call.loadNextPage()
                    onSuccess()
                } catch (e: Exception) {
                    onError(e)
                }
            }
        }
    }

    fun refresh() {
        disposables += networkDetector.waitForConnection()
                .subscribe({ onRefresh() }, logger::e)
    }

    private fun onRefresh() {
        launchWithParent(dispatchers.main) {
            sendMessage(UiResource(Status.REFRESHING))
            try {
                call.refresh(Unit)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

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