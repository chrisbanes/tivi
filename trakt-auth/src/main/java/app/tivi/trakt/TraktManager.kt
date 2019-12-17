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

package app.tivi.trakt

import android.content.SharedPreferences
import androidx.core.content.edit
import app.tivi.AppNavigator
import app.tivi.actions.ShowTasks
import app.tivi.inject.ProcessLifetime
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import com.uwetrottmann.trakt5.TraktV2
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.TokenResponse

@Singleton
class TraktManager @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    @Named("app") private val appNavigator: AppNavigator,
    private val requestProvider: Provider<AuthorizationRequest>,
    private val clientAuth: Lazy<ClientAuthentication>,
    @Named("auth") private val authPrefs: SharedPreferences,
    private val showTasks: ShowTasks,
    private val logger: Logger,
    private val traktClient: Lazy<TraktV2>,
    @ProcessLifetime val processScope: CoroutineScope
) {
    private val authState = ConflatedBroadcastChannel<AuthState>()

    private val _state = ConflatedBroadcastChannel(TraktAuthState.LOGGED_OUT)
    val state: Flow<TraktAuthState>
        get() = _state.asFlow()

    init {
        // Observer which updates local state
        processScope.launch {
            authState.asFlow().collect { authState ->
                updateAuthState(authState)

                traktClient.get().apply {
                    accessToken(authState.accessToken)
                    refreshToken(authState.refreshToken)
                }
            }
        }

        // Read the auth state from prefs
        processScope.launch {
            val state = withContext(dispatchers.io) {
                readAuthState()
            }
            authState.send(state)
        }
    }

    private suspend fun updateAuthState(authState: AuthState) {
        if (authState.isAuthorized) {
            _state.send(TraktAuthState.LOGGED_IN)
        } else {
            _state.send(TraktAuthState.LOGGED_OUT)
        }
    }

    fun startAuth(requestCode: Int, authService: AuthorizationService) {
        authService.performAuthorizationRequest(
            requestProvider.get(),
            appNavigator.provideAuthHandleResponseIntent(requestCode)
        )
    }

    fun onAuthResponse(authService: AuthorizationService, response: AuthorizationResponse) {
        authService.performTokenRequest(
            response.createTokenExchangeRequest(),
            clientAuth.get(),
            ::onTokenExchangeResponse
        )
    }

    fun onAuthException(exception: AuthorizationException) {
        logger.d(exception, "AuthException")
    }

    private fun onTokenExchangeResponse(response: TokenResponse?, ex: AuthorizationException?) {
        val newState = AuthState().apply { update(response, ex) }
        processScope.launch(dispatchers.main) {
            // Update our local state
            authState.send(newState)
        }
        processScope.launch(dispatchers.io) {
            // Persist auth state
            persistAuthState(newState)
        }
        // Now trigger a sync of all shows
        showTasks.syncFollowedShowsWhenIdle()
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
