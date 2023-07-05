// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import app.tivi.util.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import me.tatarka.inject.annotations.Inject
import net.openid.appauth.AuthState as AppAuthAuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication

@Inject
class AndroidLoginToTraktInteractor(
    private val activity: Activity,
    private val loginTraktActivityResultContract: Lazy<LoginTraktActivityResultContract>,
    private val clientAuth: Lazy<ClientAuthentication>,
    private val authService: Lazy<AuthorizationService>,
    private val logger: Logger,
) : LoginToTraktInteractor {
    private lateinit var launcher: ActivityResultLauncher<Unit>

    private val resultChannel = Channel<AuthState?>()

    override fun register() {
        require(activity is ComponentActivity)

        launcher = activity.registerForActivityResult(
            loginTraktActivityResultContract.value,
        ) { result ->
            if (result != null) {
                val (response, error) = result
                when {
                    response != null -> {
                        authService.value.performTokenRequest(
                            response.createTokenExchangeRequest(),
                            clientAuth.value,
                        ) { tokenResponse, ex ->
                            val state = AppAuthAuthState()
                                .apply { update(tokenResponse, ex) }
                                .let(::AppAuthAuthStateWrapper)
                            resultChannel.trySend(state)
                        }
                    }

                    error != null -> {
                        logger.d(error) { "AuthException" }
                        resultChannel.trySend(null)
                    }
                }
            }
        }
    }

    override suspend operator fun invoke(): AuthState? {
        launcher.launch(Unit)
        return resultChannel.receiveAsFlow().first()
    }
}
