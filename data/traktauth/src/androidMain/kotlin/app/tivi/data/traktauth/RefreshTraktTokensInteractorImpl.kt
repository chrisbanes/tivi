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

@Inject
class RefreshTraktTokensInteractorImpl(
    private val traktAuthRepository: TraktAuthRepository,
    private val authStore: AuthStore,
    private val authService: Lazy<AuthorizationService>,
) : RefreshTraktTokensInteractor {
    override suspend operator fun invoke(): app.tivi.data.traktauth.AuthState? {
        val authState = authStore.get()
        if (authState is AppAuthAuthStateWrapper) {
            val newState = suspendCoroutine { cont ->
                authService.value.performTokenRequest(
                    authState.authState.createTokenRefreshRequest(),
                ) { tokenResponse, ex ->
                    val state = AuthState()
                        .apply { update(tokenResponse, ex) }
                        .let(::AppAuthAuthStateWrapper)
                    cont.resume(state)
                }
            }

            traktAuthRepository.onNewAuthState(newState)
            return newState
        }
        return null
    }
}
