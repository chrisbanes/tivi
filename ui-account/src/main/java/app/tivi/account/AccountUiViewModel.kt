/*
 * Copyright 2020 Google LLC
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

package app.tivi.account

import androidx.lifecycle.viewModelScope
import app.tivi.ReduxViewModel
import app.tivi.domain.interactors.ClearUserDetails
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountUiViewModel @Inject constructor(
    private val traktManager: TraktManager,
    observeTraktAuthState: ObserveTraktAuthState,
    observeUserDetails: ObserveUserDetails,
    private val clearUserDetails: ClearUserDetails
) : ReduxViewModel<AccountUiViewState>(
    AccountUiViewState()
) {
    init {
        viewModelScope.launch {
            observeTraktAuthState.observe()
                .distinctUntilChanged()
                .collectAndSetState { copy(authState = it) }
        }
        observeTraktAuthState(Unit)

        viewModelScope.launch {
            observeUserDetails.observe()
                .collectAndSetState { copy(user = it) }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))
    }

    fun logout() {
        viewModelScope.launch {
            traktManager.clearAuth()
            clearUserDetails(ClearUserDetails.Params("me")).collect()
        }
    }
}
