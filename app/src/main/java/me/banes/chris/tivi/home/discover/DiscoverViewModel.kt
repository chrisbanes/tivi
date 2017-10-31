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
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import me.banes.chris.tivi.AppNavigator
import me.banes.chris.tivi.calls.PopularCall
import me.banes.chris.tivi.calls.TrendingCall
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.data.entities.PopularListItem
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.data.entities.TrendingListItem
import me.banes.chris.tivi.extensions.plusAssign
import me.banes.chris.tivi.home.HomeFragmentViewModel
import me.banes.chris.tivi.home.HomeNavigator
import me.banes.chris.tivi.home.discover.DiscoverViewModel.Section.POPULAR
import me.banes.chris.tivi.home.discover.DiscoverViewModel.Section.TRENDING
import me.banes.chris.tivi.trakt.TraktManager
import me.banes.chris.tivi.util.AppRxSchedulers
import timber.log.Timber
import javax.inject.Inject

internal class DiscoverViewModel @Inject constructor(
        private val schedulers: AppRxSchedulers,
        private val popularCall: PopularCall,
        private val trendingCall: TrendingCall,
        private val navigator: HomeNavigator,
        appNavigator: AppNavigator,
        traktManager: TraktManager) : HomeFragmentViewModel(traktManager, appNavigator) {

    data class SectionPage(val section: Section, val items: List<ListItem<out Entry>>)

    enum class Section {
        TRENDING, POPULAR
    }

    val data = MutableLiveData<List<SectionPage>>()

    init {
        disposables += Flowable.zip(
                popularCall.data(0),
                trendingCall.data(0),
                BiFunction<List<PopularListItem>, List<TrendingListItem>, List<SectionPage>> { popular, trending ->
                    listOf(SectionPage(TRENDING, trending), SectionPage(POPULAR, popular))
                })
                .observeOn(schedulers.main)
                .subscribe(data::setValue, Timber::e)

        refresh()
    }

    private fun refresh() {
        disposables += popularCall.refresh(Unit)
                .subscribe(this::onSuccess, this::onRefreshError)
        disposables += trendingCall.refresh(Unit)
                .subscribe(this::onSuccess, this::onRefreshError)
    }

    private fun onSuccess() {
        // TODO nothing really to do here
    }

    private fun onRefreshError(t: Throwable) {
        Timber.e(t, "Error while refreshing")
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
