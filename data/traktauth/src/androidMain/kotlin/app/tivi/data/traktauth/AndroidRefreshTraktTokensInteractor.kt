// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
