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
import io.reactivex.rxkotlin.plusAssign
import app.tivi.data.entities.TraktUser
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktManager
import app.tivi.util.Logger
import app.tivi.util.TiviViewModel

abstract class HomeFragmentViewModel(
    private val traktManager: TraktManager,
    protected val logger: Logger
) : TiviViewModel() {

    private val _authUiState = MutableLiveData<TraktAuthState>()
    val authUiState: LiveData<TraktAuthState>
        get() = _authUiState

    val userProfileLiveData: LiveData<TraktUser> = LiveDataReactiveStreams.fromPublisher(traktManager.userObservable())

    init {
        _authUiState.value = TraktAuthState.LOGGED_OUT

        disposables += traktManager.state
                .subscribe(_authUiState::setValue, logger::e)
    }

    fun onProfileItemClicked() {
        // TODO
    }

    fun onLoginItemClicked() {
        startAuthProcess(0)
    }

    private fun startAuthProcess(requestCode: Int) {
        traktManager.startAuth(requestCode)
    }
}