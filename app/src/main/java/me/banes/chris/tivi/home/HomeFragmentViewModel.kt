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

package me.banes.chris.tivi.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import me.banes.chris.tivi.AppNavigator
import me.banes.chris.tivi.data.entities.TraktUser
import me.banes.chris.tivi.extensions.plusAssign
import me.banes.chris.tivi.trakt.TraktManager
import me.banes.chris.tivi.util.RxAwareViewModel
import net.openid.appauth.AuthState
import timber.log.Timber

abstract class HomeFragmentViewModel(
        private val traktManager: TraktManager,
        private val appNavigator: AppNavigator)
    : RxAwareViewModel() {

    enum class AuthUiState {
        LOGGED_IN, LOGGED_OUT
    }

    val authUiState = MutableLiveData<AuthUiState>()

    val userProfileLiveData: LiveData<TraktUser> = LiveDataReactiveStreams.fromPublisher(traktManager.userObservable())

    init {
        authUiState.value = AuthUiState.LOGGED_OUT

        disposables += traktManager.stateSubject.subscribe({ handleAuthState(it) }, Timber::e)
    }

    private fun handleAuthState(state: AuthState?) {
        authUiState.value =
                if (state?.isAuthorized == true) AuthUiState.LOGGED_IN
                else AuthUiState.LOGGED_OUT
    }

    fun onProfileItemClicked() {
        // TODO
    }

    fun onLoginItemClicked() {
        startAuthProcess(0)
    }

    fun onSettingsItemClicked() {
        appNavigator.startSettings()
    }

    private fun startAuthProcess(requestCode: Int) {
        traktManager.startAuth(requestCode)
    }
}