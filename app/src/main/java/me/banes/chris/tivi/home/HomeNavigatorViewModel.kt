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

package me.banes.chris.tivi.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.util.SingleLiveEvent

class HomeNavigatorViewModel : ViewModel(), HomeNavigator {

    override fun showPopular() {
        _showPopularCall.call()
    }

    override fun showTrending() {
        _showTrendingCall.call()
    }

    override fun showWatched() {
        _showWatchedCall.call()
    }

    override fun showShowDetails(show: TiviShow) {
        _showShowDetailsCall.value = show
    }

    override fun onUpClicked() {
        _upClickedCall.call()
    }

    private val _showPopularCall = SingleLiveEvent<Unit>()
    val showPopularCall: LiveData<Unit>
        get() = _showPopularCall

    private val _showTrendingCall = SingleLiveEvent<Unit>()
    val showTrendingCall: LiveData<Unit>
        get() = _showTrendingCall

    private val _showWatchedCall = SingleLiveEvent<Unit>()
    val showWatchedCall: LiveData<Unit>
        get() = _showWatchedCall

    private val _showShowDetailsCall = SingleLiveEvent<TiviShow>()
    val showShowDetailsCall: LiveData<TiviShow>
        get() = _showShowDetailsCall

    private val _upClickedCall = SingleLiveEvent<Unit>()
    val upClickedCall: LiveData<Unit>
        get() = _upClickedCall
}
