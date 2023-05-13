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

package app.tivi.data.traktauth

import app.tivi.data.traktauth.store.AuthStore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import me.tatarka.inject.annotations.Inject
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.TokenRequest

@Inject
class AndroidRefreshTraktTokensInteractor(
    private val traktAuthRepository: Lazy<TraktAuthRepository>,
    private val authStore: Lazy<AuthStore>,
    private val authService: Lazy<AuthorizationService>,
    private val authServiceConfig: Lazy<AuthorizationServiceConfiguration>,
    private val info: TraktOAuthInfo,
) : RefreshTraktTokensInteractor {
    override suspend operator fun invoke(): app.tivi.data.traktauth.AuthState? {
        val authState = authStore.value.get()
        if (authState is AppAuthAuthStateWrapper) {
            val newState = suspendCoroutine { cont ->
                authService.value.performTokenRequest(
                    TokenRequest.Builder(authServiceConfig.value, info.clientId)
                        .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                        .setScope(null)
                        .setRefreshToken(authState.refreshToken)
                        // Disable PKCE since Trakt does not support it
                        .setCodeVerifier(null)
                        .build(),
                ) { tokenResponse, ex ->
                    val state = AuthState()
                        .apply { update(tokenResponse, ex) }
                        .let(::AppAuthAuthStateWrapper)
                    cont.resume(state)
                }
            }

            traktAuthRepository.value.onNewAuthState(newState)
            return newState
        }
        return null
    }
}
