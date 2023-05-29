// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toUri
import me.tatarka.inject.annotations.Inject
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

@Inject
class LoginTraktActivityResultContract(
    private val authService: Lazy<AuthorizationService>,
    private val authServiceConfig: Lazy<AuthorizationServiceConfiguration>,
    private val oAuthInfo: TraktOAuthInfo,
) : ActivityResultContract<Unit, LoginTraktActivityResultContract.Result?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return authService.value.getAuthorizationRequestIntent(
            AuthorizationRequest.Builder(
                authServiceConfig.value,
                oAuthInfo.clientId,
                ResponseTypeValues.CODE,
                oAuthInfo.redirectUri.toUri(),
            ).apply {
                // Disable PKCE since Trakt does not support it
                setCodeVerifier(null)
            }.build(),
        )
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): Result? = intent?.let {
        Result(
            response = AuthorizationResponse.fromIntent(it),
            exception = AuthorizationException.fromIntent(it),
        )
    }

    data class Result(
        val response: AuthorizationResponse?,
        val exception: AuthorizationException?,
    )
}
