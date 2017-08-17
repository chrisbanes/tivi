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
 *
 */

package me.banes.chris.tivi.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.banes.chris.tivi.data.TraktUser
import me.banes.chris.tivi.home.HomeActivityViewModel.AuthUiState.*
import me.banes.chris.tivi.trakt.TraktManager
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import javax.inject.Inject

internal class HomeActivityViewModel @Inject constructor(
        private val traktManager: TraktManager) : ViewModel() {

    enum class NavigationItem {
        DISCOVER, LIBRARY
    }

    enum class AuthUiState {
        LOGGED_IN, LOGGED_OUT
    }

    val currentNavigationItemLiveData = MutableLiveData<NavigationItem>()
    val authUiState = MutableLiveData<AuthUiState>()

    val userProfileLifeProfile: LiveData<TraktUser> = LiveDataReactiveStreams.fromPublisher(traktManager.userObservable())

    init {
        // Set default value
        currentNavigationItemLiveData.value = NavigationItem.DISCOVER
        authUiState.value = LOGGED_OUT

        traktManager.stateObservable.observeForever {
            authUiState.value = if (it?.isAuthorized == true) LOGGED_IN else LOGGED_OUT
        }
    }

    fun startAuthProcess(requestCode: Int) {
        traktManager.startAuth(requestCode)
    }

    fun onAuthResponse(response: AuthorizationResponse?, ex: AuthorizationException?) {
        if (ex != null) {
            traktManager.onAuthException(ex)
        } else {
            if (response != null) {
                traktManager.onAuthResponse(response)
            }
        }
    }

}
