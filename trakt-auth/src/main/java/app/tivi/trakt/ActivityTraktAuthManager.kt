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

package app.tivi.trakt

import android.content.Context
import android.content.Intent
import app.tivi.util.Logger
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication
import javax.inject.Inject

internal class ActivityTraktAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val traktManager: TraktManager,
    private val requestProvider: Lazy<AuthorizationRequest>,
    private val clientAuth: Lazy<ClientAuthentication>,
    private val logger: Logger
) : TraktAuthManager {
    private val authService by lazy(LazyThreadSafetyMode.NONE) {
        AuthorizationService(context)
    }

    override fun buildLoginIntent(): Intent {
        return authService.getAuthorizationRequestIntent(requestProvider.get())
    }

    override fun onAuthResponse(result: LoginTrakt.Result) {
        val (response, error) = result
        when {
            response != null -> {
                authService.performTokenRequest(
                    response.createTokenExchangeRequest(),
                    clientAuth.get()
                ) { tokenResponse, ex ->
                    val state = AuthState().apply {
                        update(tokenResponse, ex)
                    }
                    traktManager.onNewAuthState(state)
                }
            }
            error != null -> logger.d(error, "AuthException")
        }
    }
}
