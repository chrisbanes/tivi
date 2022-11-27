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
import app.tivi.domain.interactors.ClearTraktAuthState
import app.tivi.domain.interactors.UpdateUserDetails
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    observeTraktAuthState: ObserveTraktAuthState,
    private val updateUserDetails: UpdateUserDetails,
    observeUserDetails: ObserveUserDetails,
    private val clearTraktAuthState: ClearTraktAuthState,
    private val logger: Logger,
) : ViewModel() {
    init {
        viewModelScope.launch {
            observeUserDetails.flow.collect { user ->
                logger.setUserId(user?.username ?: "")
            }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        viewModelScope.launch {
            observeTraktAuthState.flow.collect { state ->
                if (state == TraktAuthState.LOGGED_IN) refreshMe()
            }
        }
        observeTraktAuthState(Unit)
    }

    private fun refreshMe() {
        viewModelScope.launch {
            try {
                updateUserDetails.executeSync(UpdateUserDetails.Params("me", false))
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    // If we got a 401 back from Trakt, we should clear out the auth state
                    clearTraktAuthState.executeSync(Unit)
                }
            } catch (t: Throwable) {
                // no-op
            }
        }
    }
}
