// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.trakt

import app.moviebase.trakt.Trakt
import app.tivi.app.ApplicationInfo
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.inject.ApplicationScope
import app.tivi.util.Logger
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpStatusCode
import me.tatarka.inject.annotations.Provides

actual interface TraktPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideTrakt(
    oauthInfo: TraktOAuthInfo,
    applicationInfo: ApplicationInfo,
    tiviLogger: Logger,
    traktAuthRepository: Lazy<TraktAuthRepository>,
  ): Trakt = Trakt {
    traktApiKey = oauthInfo.clientId
    maxRetries = 3

    logging {
      logger = object : io.ktor.client.plugins.logging.Logger {
        override fun log(message: String) {
          tiviLogger.d { message }
        }
      }
      level = when {
        applicationInfo.debugBuild -> LogLevel.HEADERS
        else -> LogLevel.NONE
      }
    }

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
