// Copyright 2019, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.trakt

import app.moviebase.trakt.Trakt
import app.tivi.data.traktauth.TraktAuthRepository
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
    traktAuthRepository: Lazy<TraktAuthRepository>,
  ): Trakt = Trakt {
    traktApiKey = oauthInfo.clientId
    maxRetries = 3

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
            traktAuthRepository.value.getAuthState()
              ?.let { BearerTokens(it.accessToken, it.refreshToken) }
          }

          refreshTokens {
            traktAuthRepository.value.refreshTokens()
              ?.let { BearerTokens(it.accessToken, it.refreshToken) }
          }

          sendWithoutRequest { request ->
            request.url.host == "api.trakt.tv"
          }
        }
      }
    }
  }
}
