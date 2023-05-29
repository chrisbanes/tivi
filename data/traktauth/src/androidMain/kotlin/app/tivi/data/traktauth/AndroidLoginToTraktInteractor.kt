// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import app.tivi.util.Logger
import me.tatarka.inject.annotations.Inject
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication

@Inject
class AndroidLoginToTraktInteractor(
    private val activity: Activity,
    private val loginTraktActivityResultContract: Lazy<LoginTraktActivityResultContract>,
    private val traktAuthRepository: Lazy<TraktAuthRepository>,
    private val clientAuth: Lazy<ClientAuthentication>,
    private val logger: Logger,
    private val authService: Lazy<AuthorizationService>,
) : LoginToTraktInteractor {

    private lateinit var launcher: ActivityResultLauncher<Unit>

    override fun register() {
        require(activity is ComponentActivity)

        launcher = activity.registerForActivityResult(
            loginTraktActivityResultContract.value,
        ) { result ->
            if (result != null) {
                onLoginResult(result)
            }
        }
    }

    override fun launch() = launcher.launch(Unit)

    private fun onLoginResult(result: LoginTraktActivityResultContract.Result) {
        val (response, error) = result
        when {
            response != null -> {
                authService.value.performTokenRequest(
                    response.createTokenExchangeRequest(),
                    clientAuth.value,
                ) { tokenResponse, ex ->
                    val state = AuthState()
                        .apply { update(tokenResponse, ex) }
                        .let(::AppAuthAuthStateWrapper)
                    traktAuthRepository.value.onNewAuthState(state)
                }
            }

            error != null -> logger.d(error, "AuthException")
        }
    }
}
