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
import app.tivi.TiviMvRxViewModel
import app.tivi.domain.interactors.UpdateUserDetails
import app.tivi.domain.invoke
import app.tivi.domain.launchObserve
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.domain.observers.ObserveUserDetails
import app.tivi.home.main.HomeActivityViewState
import app.tivi.home.main.HomeNavigationItem
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

class HomeActivityViewModel @AssistedInject constructor(
    @Assisted initialState: HomeActivityViewState,
    observeTraktAuthState: ObserveTraktAuthState,
    private val traktManager: TraktManager,
    private val updateUserDetails: UpdateUserDetails,
    observeUserDetails: ObserveUserDetails
) : TiviMvRxViewModel<HomeActivityViewState>(initialState) {
    init {
        viewModelScope.launchObserve(observeUserDetails) {
            it.execute { copy(user = it()) }
        }

        setState {
            copy(navigationItems = listOf(
                    HomeNavigationItem.DISCOVER,
                    HomeNavigationItem.FOLLOWED,
                    HomeNavigationItem.WATCHED,
                    HomeNavigationItem.SEARCH
            ))
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
