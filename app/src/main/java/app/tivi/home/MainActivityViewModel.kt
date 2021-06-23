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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tivi.domain.interactors.UpdateUserDetails
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    observeTraktAuthState: ObserveTraktAuthState,
    private val updateUserDetails: UpdateUserDetails,
    observeUserDetails: ObserveUserDetails,
    private val logger: Logger
) : ViewModel() {
    init {
        viewModelScope.launch {
            observeUserDetails.observe().collect { user ->
                logger.setUserId(user?.username ?: "")
            }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        viewModelScope.launch {
            observeTraktAuthState.observe().collect { state ->
                if (state == TraktAuthState.LOGGED_IN) refreshMe()
            }
        }
        observeTraktAuthState(Unit)
    }

    private fun refreshMe() {
        viewModelScope.launch {
            updateUserDetails.executeSync(UpdateUserDetails.Params("me", false))
        }
    }
}
