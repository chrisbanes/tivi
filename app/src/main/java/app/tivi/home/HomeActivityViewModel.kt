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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import app.tivi.ReduxViewModel
import app.tivi.domain.interactors.UpdateUserDetails
import app.tivi.domain.invoke
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.Logger
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

class HomeActivityViewModel @ViewModelInject constructor(
    observeTraktAuthState: ObserveTraktAuthState,
    private val traktManager: TraktManager,
    private val updateUserDetails: UpdateUserDetails,
    observeUserDetails: ObserveUserDetails,
    private val logger: Logger
) : ReduxViewModel<HomeActivityViewState>() {
    init {
        viewModelScope.launchObserve(observeUserDetails) {
            it.execute {
                copy(user = it())
            }
        }
        observeUserDetails(ObserveUserDetails.Params("me"))

        viewModelScope.launchObserve(observeTraktAuthState) { flow ->
            flow.distinctUntilChanged().onEach {
                if (it == TraktAuthState.LOGGED_IN) {
                    updateUserDetails(UpdateUserDetails.Params("me", false))
                }
            }.execute { copy(authState = it() ?: TraktAuthState.LOGGED_OUT) }
        }
        observeTraktAuthState()

        viewModelScope.launch {
            selectSubscribe(HomeActivityViewState::user).collect { user ->
                logger.setUserId(user?.username ?: "")
            }
        }
    }

    fun onAuthResponse(
        authService: AuthorizationService,
        response: AuthorizationResponse?,
        ex: AuthorizationException?
    ) {
        when {
            response != null -> traktManager.onAuthResponse(authService, response)
            ex != null -> traktManager.onAuthException(ex)
        }
    }

    fun onLoginItemClicked(authService: AuthorizationService) {
        traktManager.startAuth(0, authService)
    }

    override fun createInitialState(): HomeActivityViewState {
        return HomeActivityViewState()
    }
}
