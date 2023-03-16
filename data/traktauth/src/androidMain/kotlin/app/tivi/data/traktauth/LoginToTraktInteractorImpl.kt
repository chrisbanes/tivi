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

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import app.tivi.util.Logger
import me.tatarka.inject.annotations.Inject
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication

@Inject
class LoginToTraktInteractorImpl(
    private val activity: Activity,
    private val loginTraktActivityResultContract: LoginTraktActivityResultContract,
    private val traktAuthRepository: TraktAuthRepository,
    private val clientAuth: Lazy<ClientAuthentication>,
    private val logger: Logger,
    private val authService: Lazy<AuthorizationService>,
) : LoginToTraktInteractor {

    private lateinit var launcher: ActivityResultLauncher<Unit>

    override fun register() {
        require(activity is ComponentActivity)

        launcher = activity.registerForActivityResult(loginTraktActivityResultContract) { result ->
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
                    traktAuthRepository.onNewAuthState(state)
                }
            }

            error != null -> logger.d(error, "AuthException")
        }
    }
}
