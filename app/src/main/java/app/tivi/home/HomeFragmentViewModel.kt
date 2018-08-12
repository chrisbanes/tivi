/*
 * Copyright 2017 Google, Inc.
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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import app.tivi.data.entities.TraktUser
import app.tivi.interactors.UpdateUserDetails
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.Logger
import app.tivi.util.NetworkDetector
import app.tivi.util.TiviViewModel
import io.reactivex.rxkotlin.plusAssign
import net.openid.appauth.AuthorizationService

abstract class HomeFragmentViewModel(
    private val traktManager: TraktManager,
    private val updateUserDetails: UpdateUserDetails,
    private val networkDetector: NetworkDetector,
    protected val logger: Logger
) : TiviViewModel() {

    private val _authUiState = MutableLiveData<TraktAuthState>()
    val authUiState: LiveData<TraktAuthState>
        get() = _authUiState

    val userProfileLiveData: LiveData<TraktUser> = LiveDataReactiveStreams.fromPublisher(updateUserDetails.observe())

    init {
        _authUiState.value = TraktAuthState.LOGGED_OUT

        disposables += traktManager.state
                .distinctUntilChanged()
                .subscribe(::onAuthStateChanged, logger::e)

        updateUserDetails.setParams(UpdateUserDetails.Params("me"))
    }

    private fun onAuthStateChanged(authState: TraktAuthState) {
        _authUiState.value = authState

        if (authState == TraktAuthState.LOGGED_IN) {
            disposables += networkDetector.waitForConnection()
                    .subscribe({ refreshUserProfile() }, logger::e)
        }
    }

    private fun refreshUserProfile() {
        launchInteractor(updateUserDetails, UpdateUserDetails.ExecuteParams(false))
    }

    fun onProfileItemClicked() {
        // TODO
    }

    fun onLoginItemClicked(authService: AuthorizationService) {
        traktManager.startAuth(0, authService)
    }
}