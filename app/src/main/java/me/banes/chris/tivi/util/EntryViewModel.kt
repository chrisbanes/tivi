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

package me.banes.chris.tivi.util

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import io.reactivex.BackpressureStrategy
import io.reactivex.rxkotlin.Flowables
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.experimental.launch
import me.banes.chris.tivi.api.Resource
import me.banes.chris.tivi.api.Status
import me.banes.chris.tivi.calls.ListCall
import me.banes.chris.tivi.calls.PaginatedCall
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.tmdb.TmdbManager
import timber.log.Timber

open class EntryViewModel<LI : ListItem<out Entry>>(
    private val schedulers: AppRxSchedulers,
    private val coroutineDispatchers: AppCoroutineDispatchers,
    private val call: ListCall<Unit, LI>,
    tmdbManager: TmdbManager,
    refreshOnStartup: Boolean = true
) : RxAwareViewModel() {

    private val messages = BehaviorSubject.create<Resource>()

    val liveList by lazy(mode = LazyThreadSafetyMode.NONE) {
        LivePagedListBuilder<Int, LI>(
                call.dataSourceFactory(),
                PagedList.Config.Builder().run {
                    setPageSize(call.pageSize)
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
        // Eagerly refresh the initial page of trending
        if (refreshOnStartup) {
            fullRefresh()
        }
    }

    fun onListScrolledToEnd() {
        if (call is PaginatedCall<*, *>) {
            launch {
                launch(coroutineDispatchers.main) {
                    sendMessage(Resource(Status.LOADING_MORE))
                }
                try {
                    call.loadNextPage()

                    launch(coroutineDispatchers.main) {
                        onSuccess()
                    }
                } catch (e: Exception) {
                    launch(coroutineDispatchers.main) {
                        onError(e)
                    }
                }
            }
        }
    }

    fun fullRefresh() {
        launch {
            launch(coroutineDispatchers.main) {
                sendMessage(Resource(Status.REFRESHING))
            }
            try {
                call.refresh(Unit)

                launch(coroutineDispatchers.main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                launch(coroutineDispatchers.main) {
                    onError(e)
                }
            }
        }
    }

    private fun onError(t: Throwable) {
        Timber.e(t)
        sendMessage(Resource(Status.ERROR, t.localizedMessage))
    }

    private fun onSuccess() {
        sendMessage(Resource(Status.SUCCESS))
    }

    private fun sendMessage(resource: Resource) {
        messages.onNext(resource)
    }
}