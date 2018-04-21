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

package me.banes.chris.tivi.trakt

import android.content.SharedPreferences
import androidx.core.content.edit
import com.uwetrottmann.trakt5.TraktV2
import dagger.Lazy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.experimental.launch
import me.banes.chris.tivi.AppNavigator
import me.banes.chris.tivi.data.entities.TraktUser
import me.banes.chris.tivi.inject.ApplicationLevel
import me.banes.chris.tivi.trakt.calls.UserMeCall
import me.banes.chris.tivi.util.AppRxSchedulers
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TraktManager @Inject constructor(
    private val schedulers: AppRxSchedulers,
    @ApplicationLevel private val disposables: CompositeDisposable,
    @Named("app") private val appNavigator: AppNavigator,
    private val requestProvider: Provider<AuthorizationRequest>,
    private val authService: AuthorizationService,
    private val clientAuth: Lazy<ClientAuthentication>,
    @Named("auth") private val authPrefs: SharedPreferences,
    private val traktClient: TraktV2,
    private val userMeCall: UserMeCall
) {
    val stateSubject = BehaviorSubject.create<AuthState>()!!

    init {
        // First observer updates the local storage
        disposables += stateSubject.observeOn(schedulers.database)
                .subscribe(this::persistAuthState, Timber::e)

        // Second observer updates local state
        disposables += stateSubject.observeOn(schedulers.main)
                .subscribe({
                    traktClient.accessToken(it.accessToken)
                    traktClient.refreshToken(it.refreshToken)
                    if (it.isAuthorized) {
                        // Now refresh the user information
                        launch {
                            userMeCall.refresh(Unit)
                        }
                    }
                }, Timber::e)

        // Read the auth state from database
        disposables += Maybe.fromCallable(this::readAuthState)
                .subscribeOn(schedulers.database)
                .observeOn(schedulers.main)
                .subscribe(stateSubject::onNext, stateSubject::onError)
    }

    fun startAuth(requestCode: Int) {
        authService.performAuthorizationRequest(
                requestProvider.get(),
                appNavigator.provideAuthHandleResponseIntent(requestCode))
    }

    fun onAuthResponse(response: AuthorizationResponse) {
        performTokenExchange(response)
    }

    fun onAuthException(exception: AuthorizationException) {
        Timber.d(exception, "AuthException")
    }

    fun userObservable(): Flowable<TraktUser> = userMeCall.data()

    private fun performTokenExchange(response: AuthorizationResponse) {
        authService.performTokenRequest(response.createTokenExchangeRequest(), clientAuth.get()) { tokenResponse, exception ->
            val newState = AuthState().apply {
                update(tokenResponse, exception)
            }
            stateSubject.onNext(newState)
        }
    }

    private fun readAuthState(): AuthState {
        val stateJson = authPrefs.getString("stateJson", null)
        return when {
            stateJson != null -> AuthState.jsonDeserialize(stateJson)
            else -> AuthState()
        }
    }

    private fun persistAuthState(state: AuthState) {
        authPrefs.edit {
            putString("stateJson", state.jsonSerializeString())
        }
    }
}
