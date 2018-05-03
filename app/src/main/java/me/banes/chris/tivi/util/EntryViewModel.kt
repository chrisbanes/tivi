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

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.selects.select
import me.banes.chris.tivi.api.Status
import me.banes.chris.tivi.api.UiResource
import me.banes.chris.tivi.calls.ListCall
import me.banes.chris.tivi.calls.PaginatedCall
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.tmdb.TmdbManager
import timber.log.Timber

open class EntryViewModel<LI : ListItem<out Entry>>(
    private val dispatchers: AppCoroutineDispatchers,
    private val call: ListCall<Unit, LI>,
    tmdbManager: TmdbManager,
    refreshOnStartup: Boolean = true
) : TiviViewModel() {

    private val messages = ConflatedBroadcastChannel<UiResource>()

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

    val viewState = MutableLiveData<EntryViewState>()

    init {
        launchWithParent(dispatchers.main, CoroutineStart.UNDISPATCHED) {
            var model = EntryViewState()
            while (isActive) {
                model = select {
                    messages.openSubscription().onReceive {
                        model.copy(uiResource = it)
                    }
                    tmdbManager.imageProvider.onReceive {
                        model.copy(tmdbImageUrlProvider = it)
                    }
                }
                viewState.value = model
            }
        }

        // Eagerly refresh the initial page of trending
        if (refreshOnStartup) {
            fullRefresh()
        }
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

    fun fullRefresh() {
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
        Timber.e(t)
        sendMessage(UiResource(Status.ERROR, t.localizedMessage))
    }

    private fun onSuccess() {
        sendMessage(UiResource(Status.SUCCESS))
    }

    private fun sendMessage(uiResource: UiResource) {
        messages.offer(uiResource)
    }
}