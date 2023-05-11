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

import app.moviebase.trakt.Trakt
import app.tivi.data.traktauth.RefreshTraktTokensInteractor
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.data.traktauth.store.AuthStore
import app.tivi.inject.ApplicationScope
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.http.HttpStatusCode
import me.tatarka.inject.annotations.Provides
import okhttp3.OkHttpClient

actual interface TraktPlatformComponent {
    @ApplicationScope
    @Provides
    fun provideTrakt(
        client: OkHttpClient,
        authStore: AuthStore,
        oauthInfo: TraktOAuthInfo,
        refreshTokens: Lazy<RefreshTraktTokensInteractor>,
    ): Trakt = Trakt {
        traktApiKey = oauthInfo.clientId
        maxRetriesOnException = 3

        httpClient(OkHttp) {
            // Probably want to move to using Ktor's caching, timeouts, etc eventually
            engine {
                preconfigured = client
            }

            install(HttpRequestRetry) {
                retryIf(5) { _, httpResponse ->
                    when {
                        httpResponse.status.value in 500..599 -> true
                        httpResponse.status == HttpStatusCode.TooManyRequests -> true
                        else -> false
                    }
                }
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        authStore.get()?.let {
                            BearerTokens(it.accessToken, it.refreshToken)
                        }
                    }

                    refreshTokens {
                        refreshTokens.value.invoke()?.let {
                            BearerTokens(it.accessToken, it.refreshToken)
                        }
                    }

                    sendWithoutRequest { request ->
                        request.url.host == "api.trakt.tv"
                    }
                }
            }
        }
    }
}
