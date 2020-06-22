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

package app.tivi.home

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import app.tivi.AppNavigator
import app.tivi.trakt.TraktConstants
import app.tivi.trakt.TraktManager
import app.tivi.util.Logger
import dagger.Lazy
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication
import javax.inject.Inject

class ActivityAppNavigator @Inject constructor(
    private val activity: Activity,
    private val traktManager: TraktManager,
    private val requestProvider: Lazy<AuthorizationRequest>,
    private val clientAuth: Lazy<ClientAuthentication>,
    private val logger: Logger
) : AppNavigator {
    private val authService by lazy(LazyThreadSafetyMode.NONE) {
        AuthorizationService(activity)
    }

    override fun login() {
        authService.performAuthorizationRequest(
            requestProvider.get(),
            provideAuthHandleResponseIntent(0)
        )
    }

    override fun onAuthResponse(intent: Intent) {
        val response = AuthorizationResponse.fromIntent(intent)
        val error = AuthorizationException.fromIntent(intent)
        when {
            response != null -> {
                authService.performTokenRequest(
                    response.createTokenExchangeRequest(),
                    clientAuth.get()
                ) { tokenResponse, ex ->
                    val state = AuthState().apply { update(tokenResponse, ex) }
                    traktManager.onNewAuthState(state)
                }
            }
            error != null -> logger.d(error, "AuthException")
        }
    }

    private fun provideAuthHandleResponseIntent(requestCode: Int): PendingIntent {
        val intent = Intent(activity, MainActivity::class.java).apply {
            action = TraktConstants.INTENT_ACTION_HANDLE_AUTH_RESPONSE
        }
        return PendingIntent.getActivity(activity, requestCode, intent, 0)
    }
}
