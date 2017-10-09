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

package me.banes.chris.tivi.home.discover

import android.arch.lifecycle.MutableLiveData
import me.banes.chris.tivi.AppNavigator
import me.banes.chris.tivi.calls.PopularCall
import me.banes.chris.tivi.calls.TrendingCall
import me.banes.chris.tivi.data.TiviShow
import me.banes.chris.tivi.extensions.plusAssign
import me.banes.chris.tivi.home.HomeFragmentViewModel
import me.banes.chris.tivi.home.HomeNavigator
import me.banes.chris.tivi.home.discover.DiscoverViewModel.Section.POPULAR
import me.banes.chris.tivi.home.discover.DiscoverViewModel.Section.TRENDING
import me.banes.chris.tivi.trakt.TraktManager
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject

internal class DiscoverViewModel @Inject constructor(
        private val schedulers: AppRxSchedulers,
        private val popularCall: PopularCall,
        private val trendingCall: TrendingCall,
        private val navigator: HomeNavigator,
        appNavigator: AppNavigator,
        traktManager: TraktManager) : HomeFragmentViewModel(traktManager, appNavigator) {

    private val items = mapOf(
            TRENDING to mutableListOf<TiviShow>(),
            POPULAR to mutableListOf<TiviShow>())

    enum class Section {
        TRENDING, POPULAR
    }

    val data = MutableLiveData<Map<Section, List<TiviShow>>>()

    init {
        data.value = items

        disposables += popularCall.data(0)
                .observeOn(schedulers.main)
                .subscribe {
                    items[POPULAR]?.apply {
                        clear()
                        addAll(it)
                    }
                    data.value = items
                }

        disposables += trendingCall.data(0)
                .observeOn(schedulers.main)
                .subscribe {
                    items[TRENDING]?.apply {
                        clear()
                        addAll(it)
                    }
                    data.value = items
                }

        refresh()
    }

    private fun refresh() {
        disposables += popularCall.refresh(Unit).subscribe()
        disposables += trendingCall.refresh(Unit).subscribe()
    }

    fun onSectionHeaderClicked(section: Section) {
        when (section) {
            TRENDING -> navigator.showTrending()
            POPULAR -> navigator.showPopular()
        }
    }

    fun onItemPostedClicked(show: TiviShow) {
        navigator.showShowDetails(show)
    }

}
