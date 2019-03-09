/*
 * Copyright 2019 Google LLC
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

package app.tivi.home.main

import app.tivi.home.HomeNavigator
import app.tivi.interactors.UpdateUserDetails
import app.tivi.interactors.launchInteractor
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.TiviMvRxViewModel
import com.airbnb.mvrx.FragmentViewModelContext
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import net.openid.appauth.AuthorizationService

class HomeNavigationViewModel @AssistedInject constructor(
    @Assisted initialState: HomeNavigationViewState,
    private val traktManager: TraktManager,
    private val updateUserDetails: UpdateUserDetails
) : TiviMvRxViewModel<HomeNavigationViewState>(initialState) {

    init {
        updateUserDetails.setParams(UpdateUserDetails.Params("me"))
        updateUserDetails.observe()
                .toObservable()
                .execute { copy(user = it()) }

        traktManager.state
                .distinctUntilChanged()
                .doOnNext {
                    if (it == TraktAuthState.LOGGED_IN) {
                        scope.launchInteractor(updateUserDetails, UpdateUserDetails.ExecuteParams(false))
                    }
                }.execute {
                    copy(authState = it() ?: TraktAuthState.LOGGED_OUT)
                }
    }

    fun onNavigationItemSelected(item: HomeNavigationItem) {
        setState {
            copy(currentNavigationItem = item)
        }
    }

    fun onProfileItemClicked() {
        // TODO
    }

    fun onLoginItemClicked(authService: AuthorizationService) {
        traktManager.startAuth(0, authService)
    }

    fun onSettingsClicked(navigator: HomeNavigator) = navigator.showSettings()

    @AssistedInject.Factory
    interface Factory {
        fun create(initialState: HomeNavigationViewState): HomeNavigationViewModel
    }

    companion object : MvRxViewModelFactory<HomeNavigationViewModel, HomeNavigationViewState> {
        override fun create(viewModelContext: ViewModelContext, state: HomeNavigationViewState): HomeNavigationViewModel? {
            val fragment: HomeNavigationFragment = (viewModelContext as FragmentViewModelContext).fragment()
            return fragment.homeNavigationViewModelFactory.create(state)
        }
    }
}
