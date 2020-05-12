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

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import app.tivi.AppNavigator
import app.tivi.TiviMvRxViewModel
import app.tivi.domain.invoke
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import javax.inject.Provider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class AccountUiViewModel @AssistedInject constructor(
    @Assisted initialState: AccountUiViewState,
    private val traktManager: TraktManager,
    observeTraktAuthState: ObserveTraktAuthState,
    observeUserDetails: ObserveUserDetails,
    private val appNavigator: Provider<AppNavigator>
) : TiviMvRxViewModel<AccountUiViewState>(initialState) {
    private val pendingActions = Channel<AccountUiAction>(Channel.BUFFERED)

    init {
        viewModelScope.launchObserve(observeUserDetails) { flow ->
            flow.execute { copy(user = it()) }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        viewModelScope.launchObserve(observeTraktAuthState) { flow ->
            flow.distinctUntilChanged().execute {
                copy(authState = it() ?: TraktAuthState.LOGGED_OUT)
            }
        }
        observeTraktAuthState()

        viewModelScope.launch {
            for (action in pendingActions) {
                when (action) {
                    Login -> appNavigator.get().login()
                    Logout -> logout()
                }
            }
        }
    }

    fun submitAction(action: AccountUiAction) {
        viewModelScope.launch {
            pendingActions.send(action)
        }
    }

    private fun logout() {
        viewModelScope.launch {
            traktManager.clearAuth()
        }
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: AccountUiViewState): AccountUiViewModel
    }

    interface FactoryProvider {
        fun provideFactory(): Factory
    }

    companion object : MvRxViewModelFactory<AccountUiViewModel, AccountUiViewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: AccountUiViewState
        ): AccountUiViewModel? {
            val fragment: Fragment = (viewModelContext as FragmentViewModelContext).fragment()
            return (fragment as FactoryProvider).provideFactory().create(state)
        }
    }
}
