// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.trakt

import app.moviebase.trakt.Trakt
import app.tivi.data.traktauth.RefreshTraktTokensInteractor
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.data.traktauth.store.AuthStore
import app.tivi.inject.ApplicationScope
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.http.HttpStatusCode
import me.tatarka.inject.annotations.Provides

actual interface TraktPlatformComponent {
    @ApplicationScope
    @Provides
    fun provideTrakt(
        authStore: AuthStore,
        oauthInfo: TraktOAuthInfo,
        refreshTokens: Lazy<RefreshTraktTokensInteractor>,
    ): Trakt = Trakt {
        traktApiKey = oauthInfo.clientId
        maxRetriesOnException = 3

        httpClient(Darwin) {
            engine {
                configureRequest {
                    setAllowsCellularAccess(true)
                }
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
