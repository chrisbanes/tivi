// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import co.touchlab.kermit.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import me.tatarka.inject.annotations.Inject
import net.openid.appauth.AuthState as AppAuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.TokenRequest

@Inject
class AndroidTraktRefreshTokenAction(
  private val authService: Lazy<AuthorizationService>,
  private val authServiceConfig: Lazy<AuthorizationServiceConfiguration>,
  private val info: TraktOAuthInfo,
) : TraktRefreshTokenAction {
  override suspend operator fun invoke(state: AuthState): AuthState? = suspendCoroutine { cont ->
    authService.value.performTokenRequest(
      TokenRequest.Builder(authServiceConfig.value, info.clientId)
        .setGrantType(GrantTypeValues.REFRESH_TOKEN)
        .setScope(null)
        .setRefreshToken(state.refreshToken)
        // Disable PKCE since Trakt does not support it
        .setCodeVerifier(null)
        .build(),
    ) { tokenResponse, ex ->
      if (ex != null) {
        Logger.d("AndroidTraktRefreshTokenAction", ex) { "Error whilst calling performTokenRequest" }
        cont.resumeWithException(ex)
      } else {
        val newState = AppAuthState()
          .apply { update(tokenResponse, null) }
          .let(::AppAuthAuthStateWrapper)
        cont.resume(newState)
      }
    }
  }
}
