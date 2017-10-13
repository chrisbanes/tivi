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
import me.banes.chris.tivi.calls.Call
import me.banes.chris.tivi.calls.PaginatedCall
import me.banes.chris.tivi.calls.TmdbShowFetcher
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.extensions.plusAssign

open class EntryViewModel<LI : ListItem<out Entry>>(
        val schedulers: AppRxSchedulers,
        val call: Call<Unit, List<LI>>,
        private val tmdbShowFetcher: TmdbShowFetcher,
        refreshOnStartup: Boolean) : RxAwareViewModel() {

    /**
     * This is what my UI (Fragment) observes. Its backed by Room and a network call
     */
    val data: LiveData<List<LI>> by lazy(mode = LazyThreadSafetyMode.NONE) {
        val updateCall = call.data().doOnNext {
            it.forEach {
                it?.shows?.get(0)?.let {
                    if (it.needsUpdateFromTmdb()) {
                        val fetcher = tmdbShowFetcher.getShow(it.tmdbId!!)
                        fetcher?.let {
                            disposables += fetcher.subscribe({}, this::onError)
                        }
                    }
                }
            }
        }
        ReactiveLiveData(updateCall)
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
                    .subscribe({ messages.value = Resource(Status.SUCCESS) }, this::onError)
        }
    }

    private fun onError(t: Throwable) {
        messages.value = Resource(Status.ERROR, t.localizedMessage)
    }

    fun fullRefresh() {
        disposables += call.refresh(Unit)
                .observeOn(schedulers.main)
                .doOnSubscribe { messages.value = Resource(Status.REFRESHING) }
                .subscribe(
                        { messages.value = Resource(Status.SUCCESS) },
                        { messages.value = Resource(Status.ERROR, it.localizedMessage) })
    }
}