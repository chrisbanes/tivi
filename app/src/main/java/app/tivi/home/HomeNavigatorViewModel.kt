/*
 * Copyright 2017 Google LLC
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

package app.tivi.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.tivi.AppNavigator
import app.tivi.SharedElementHelper
import app.tivi.data.entities.TiviShow
import app.tivi.util.Event
import javax.inject.Inject
import javax.inject.Provider

class HomeNavigatorViewModel @Inject constructor(
    private val appNavigatorProvider: Provider<AppNavigator>
) : ViewModel(), HomeNavigator {

    override fun showPopular(sharedElements: SharedElementHelper?) {
        _showPopularCall.value = Event(sharedElements)
    }

    override fun showTrending(sharedElements: SharedElementHelper?) {
        _showTrendingCall.value = Event(sharedElements)
    }

    override fun showShowDetails(show: TiviShow, sharedElements: SharedElementHelper?) {
        appNavigatorProvider.get().startShowDetails(show.id, sharedElements)
    }

    override fun showSettings() {
        appNavigatorProvider.get().startSettings()
    }

    override fun onUpClicked() {
        _upClickedCall.value = Event(Unit)
    }

    private val _showPopularCall = MutableLiveData<Event<SharedElementHelper?>>()
    val showPopularCall: LiveData<Event<SharedElementHelper?>>
        get() = _showPopularCall

    private val _showTrendingCall = MutableLiveData<Event<SharedElementHelper?>>()
    val showTrendingCall: LiveData<Event<SharedElementHelper?>>
        get() = _showTrendingCall

    private val _upClickedCall = MutableLiveData<Event<Unit>>()
    val upClickedCall: LiveData<Event<Unit>>
        get() = _upClickedCall
}
