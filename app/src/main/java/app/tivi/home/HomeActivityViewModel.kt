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

import androidx.lifecycle.viewModelScope
import app.tivi.home.main.HomeActivityViewState
import app.tivi.interactors.ObserveUserDetails
import app.tivi.interactors.UpdateUserDetails
import app.tivi.interactors.launchInteractor
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.TiviMvRxViewModel
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

class HomeActivityViewModel @AssistedInject constructor(
    @Assisted initialState: HomeActivityViewState,
    private val traktManager: TraktManager,
    private val updateUserDetails: UpdateUserDetails,
    observeUserDetails: ObserveUserDetails
) : TiviMvRxViewModel<HomeActivityViewState>(initialState) {
    init {
        viewModelScope.launch {
            observeUserDetails.observe()
                    .execute { copy(user = it()) }
        }
        viewModelScope.launch {
            observeUserDetails(ObserveUserDetails.Params("me"))
        }

        viewModelScope.launch {
            traktManager.state
                    .distinctUntilChanged()
                    .onEach {
                        if (it == TraktAuthState.LOGGED_IN) {
                            viewModelScope.launchInteractor(updateUserDetails,
                                    UpdateUserDetails.Params("me", false))
                        }
                    }
                    .execute {
                        copy(authState = it() ?: TraktAuthState.LOGGED_OUT)
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

    fun onProfileItemClicked() {
        // TODO
    }

    fun onLoginItemClicked(authService: AuthorizationService) {
        traktManager.startAuth(0, authService)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: HomeActivityViewState): HomeActivityViewModel
    }

    companion object : MvRxViewModelFactory<HomeActivityViewModel, HomeActivityViewState> {
        override fun create(viewModelContext: ViewModelContext, state: HomeActivityViewState): HomeActivityViewModel? {
            val fragment: HomeActivity = viewModelContext.activity()
            return fragment.homeNavigationViewModelFactory.create(state)
        }
    }
}
