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
import android.arch.paging.PagedList
import me.banes.chris.tivi.api.Resource
import me.banes.chris.tivi.api.Status
import me.banes.chris.tivi.calls.ListCall
import me.banes.chris.tivi.calls.PaginatedCall
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.extensions.plusAssign
import timber.log.Timber

open class EntryViewModel<LI : ListItem<out Entry>>(
        val schedulers: AppRxSchedulers,
        val call: ListCall<Unit, LI>,
        refreshOnStartup: Boolean = true) : RxAwareViewModel() {

    val liveList by lazy(mode = LazyThreadSafetyMode.NONE) {
        call.liveList().create(0,
                PagedList.Config.Builder()
                        .setPageSize(call.pageSize)
                        .setEnablePlaceholders(true)
                        .build())
    }

    val messages = MutableLiveData<Resource>()

    init {
        // Eagerly refresh the initial page of trending
        if (refreshOnStartup) {
            fullRefresh()
        }
    }

    fun onListScrolledToEnd() {
        if (call is PaginatedCall<*, *>) {
            disposables += call.loadNextPage()
                    .observeOn(schedulers.main)
                    .doOnSubscribe { messages.value = Resource(Status.LOADING_MORE) }
                    .subscribe(this::onSuccess, this::onError)
        }
    }

    fun fullRefresh() {
        disposables += call.refresh(Unit)
                .observeOn(schedulers.main)
                .doOnSubscribe { messages.value = Resource(Status.REFRESHING) }
                .subscribe(this::onSuccess, this::onError)
    }

    private fun onError(t: Throwable) {
        Timber.e(t)
        messages.value = Resource(Status.ERROR, t.localizedMessage)
    }

    private fun onSuccess() {
        messages.value = Resource(Status.SUCCESS)
    }
}