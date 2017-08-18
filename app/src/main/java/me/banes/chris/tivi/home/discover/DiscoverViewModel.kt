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
 *
 */

package me.banes.chris.tivi.home.discover

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import me.banes.chris.tivi.calls.PopularCall
import me.banes.chris.tivi.calls.TrendingCall
import me.banes.chris.tivi.data.TiviShow
import me.banes.chris.tivi.home.BaseHomeFragmentViewModel
import me.banes.chris.tivi.home.HomeNavigator
import me.banes.chris.tivi.home.discover.DiscoverViewModel.Section.*
import me.banes.chris.tivi.trakt.TraktManager
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject

internal class DiscoverViewModel @Inject constructor(
        private val schedulers: AppRxSchedulers,
        private val popularCall: PopularCall,
        private val trendingCall: TrendingCall,
        private val navigator: HomeNavigator,
        traktManager: TraktManager) : BaseHomeFragmentViewModel(traktManager) {

    private val subscriptions = CompositeDisposable()

    private val items = mapOf(
            TRENDING to mutableListOf<TiviShow>(),
            POPULAR to mutableListOf<TiviShow>())

    enum class Section {
        TRENDING, POPULAR
    }

    val data = MutableLiveData<Map<Section, List<TiviShow>>>()

    init {
        data.value = items

        subscriptions.add(popularCall.data()
                .observeOn(schedulers.main)
                .subscribe {
                    items[POPULAR]?.apply {
                        clear()
                        addAll(it)
                    }
                    data.value = items
                })

        subscriptions.add(trendingCall.data()
                .observeOn(schedulers.main)
                .subscribe {
                    items[TRENDING]?.apply {
                        clear()
                        addAll(it)
                    }
                    data.value = items
                })

        refresh()
    }

    fun refresh() {
        subscriptions.add(popularCall.refresh().subscribe())
        subscriptions.add(trendingCall.refresh().subscribe())
    }

    fun onSectionHeaderClicked(section: Section) {
        navigator.showTrending()
    }

    fun onItemPostedClicked(show: TiviShow) {
        navigator.showShowDetails(show)
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }

}
