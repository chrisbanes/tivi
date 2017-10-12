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

package me.banes.chris.tivi.home.trending

import com.uwetrottmann.trakt5.entities.TrendingShow
import me.banes.chris.tivi.calls.TrendingCall
import me.banes.chris.tivi.data.daos.TrendingDao
import me.banes.chris.tivi.data.entities.TrendingEntry
import me.banes.chris.tivi.home.HomeNavigator
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.PaginatedTraktViewModel
import javax.inject.Inject

class TrendingShowsViewModel @Inject constructor(
        schedulers: AppRxSchedulers,
        call: TrendingCall,
        private val navigator: HomeNavigator)
    : PaginatedTraktViewModel<TrendingShow, TrendingEntry, TrendingDao>(schedulers, call) {

    fun onUpClicked() {
        navigator.onUpClicked()
    }

}
