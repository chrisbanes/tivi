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
import me.banes.chris.tivi.trakt.TraktManager
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

open class BaseHomeFragmentViewModel(private val traktManager: TraktManager) : ViewModel() {

    enum class AuthUiState {
        LOGGED_IN, LOGGED_OUT
    }

    val authUiState = MutableLiveData<AuthUiState>()

    val userProfileLiveData: LiveData<TraktUser> =
            LiveDataReactiveStreams.fromPublisher(traktManager.userObservable())

    init {
        authUiState.value = AuthUiState.LOGGED_OUT

        traktManager.stateObservable.observeForever {
            authUiState.value =
                    if (it?.isAuthorized == true) AuthUiState.LOGGED_IN
                    else AuthUiState.LOGGED_OUT
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