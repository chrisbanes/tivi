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
import android.arch.lifecycle.MutableLiveData
import me.banes.chris.tivi.api.Resource
import me.banes.chris.tivi.api.Status
import me.banes.chris.tivi.calls.PaginatedTraktCall
import me.banes.chris.tivi.data.TiviShow
import me.banes.chris.tivi.extensions.plusAssign

open class PaginatedTraktViewModel<R>(
        val schedulers: AppRxSchedulers,
        val call: PaginatedTraktCall<R>) : RxAwareViewModel() {

    /**
     * This is what my UI (Fragment) observes. Its backed by Room and a network call
     */
    val data: LiveData<List<TiviShow>> by lazy {
        ReactiveLiveData(call.data())
    }

    val messages = MutableLiveData<Resource>()

    init {
        // Eagerly refresh the initial page of trending
        fullRefresh()
    }

    fun onListScrolledToEnd() {
        disposables += call.loadNextPage()
                .observeOn(schedulers.main)
                .doOnSubscribe { messages.value = Resource(Status.LOADING_MORE) }
                .doOnError { messages.value = Resource(Status.ERROR, it.localizedMessage) }
                .doOnComplete { messages.value = Resource(Status.SUCCESS) }
                .subscribe()
    }

    fun fullRefresh() {
        disposables += call.refresh()
                .observeOn(schedulers.main)
                .doOnSubscribe { messages.value = Resource(Status.REFRESHING) }
                .doOnError { messages.value = Resource(Status.ERROR, it.localizedMessage) }
                .doOnComplete { messages.value = Resource(Status.SUCCESS) }
                .subscribe()
    }
}