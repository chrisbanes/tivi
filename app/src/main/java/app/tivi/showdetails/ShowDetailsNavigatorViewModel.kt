/*
 * Copyright 2018 Google LLC
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

package app.tivi.showdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.tivi.AppNavigator
import app.tivi.SharedElementHelper
import app.tivi.data.entities.TiviShow
import app.tivi.util.Event
import javax.inject.Inject
import javax.inject.Provider

class ShowDetailsNavigatorViewModel @Inject constructor(
    private val appNavigatorProvider: Provider<AppNavigator>
) : ViewModel(), ShowDetailsNavigator {
    override fun showShowDetails(show: TiviShow, sharedElements: SharedElementHelper?) {
        appNavigatorProvider.get().startShowDetails(show.id, sharedElements)
    }

    override fun navigateUp() {
        _events.value = Event(NavigateUpEvent)
    }

    private val _events = MutableLiveData<Event<ShowDetailsNavigatorEvent>>()
    val events: LiveData<Event<ShowDetailsNavigatorEvent>>
        get() = _events
}

sealed class ShowDetailsNavigatorEvent
object NavigateUpEvent : ShowDetailsNavigatorEvent()