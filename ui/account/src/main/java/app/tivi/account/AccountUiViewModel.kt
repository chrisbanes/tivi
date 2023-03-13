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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tivi.data.traktauth.LoginToTraktInteractor
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.domain.interactors.ClearUserDetails
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class AccountUiViewModel(
    private val traktAuthRepository: TraktAuthRepository,
    private val loginToTraktInteractor: LoginToTraktInteractor,
    observeTraktAuthState: ObserveTraktAuthState,
    observeUserDetails: ObserveUserDetails,
    private val clearUserDetails: ClearUserDetails,
) : ViewModel() {

    val state: StateFlow<AccountUiViewState> = combine(
        observeTraktAuthState.flow,
        observeUserDetails.flow,
    ) { authState, user ->
        AccountUiViewState(
            user = user,
            authState = authState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = AccountUiViewState.Empty,
    )

    init {
        observeTraktAuthState(Unit)
        observeUserDetails(ObserveUserDetails.Params("me"))
    }

    fun logout() {
        viewModelScope.launch {
            traktAuthRepository.clearAuth()
            clearUserDetails(ClearUserDetails.Params("me")).collect()
        }
    }

    fun login() {
        viewModelScope.launch {
            loginToTraktInteractor.launch()
        }
    }
}
