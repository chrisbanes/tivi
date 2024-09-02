// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("invisible_reference", "invisible_member")

package app.tivi.trakt

import app.moviebase.trakt.TraktBearerTokens
import app.moviebase.trakt.TraktClientConfig
import app.moviebase.trakt.api.TraktAuthApi
import app.moviebase.trakt.api.TraktCheckinApi
import app.moviebase.trakt.api.TraktCommentsApi
import app.moviebase.trakt.api.TraktEpisodesApi
import app.moviebase.trakt.api.TraktMoviesApi
import app.moviebase.trakt.api.TraktRecommendationsApi
import app.moviebase.trakt.api.TraktSearchApi
import app.moviebase.trakt.api.TraktSeasonsApi
import app.moviebase.trakt.api.TraktShowsApi
import app.moviebase.trakt.api.TraktSyncApi
import app.moviebase.trakt.api.TraktUsersApi
import app.moviebase.trakt.core.HttpClientFactory
import app.moviebase.trakt.core.TraktDsl
import app.moviebase.trakt.core.interceptRequest
import app.tivi.app.ApplicationInfo
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktOAuthInfo
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.request.header

@TraktDsl
fun TiviTrakt(block: TraktClientConfig.() -> Unit): TiviTrakt {
  val config = TraktClientConfig().apply(block)
  return TiviTrakt(config)
}

class TiviTrakt internal constructor(private val config: TraktClientConfig) {

  private val client: HttpClient by lazy {
    HttpClientFactory.create(config = config, useAuthentication = true).apply {
      interceptRequest {
        it.header(TraktHeader.API_KEY, config.traktApiKey)
        it.header(TraktHeader.API_VERSION, TraktWebConfig.VERSION)
      }
    }
  }

  init {
    requireNotNull(config.traktApiKey) {
      "Trakt API key unavailable. Set the traktApiKey field in the class TraktClientConfig " +
        "when instantiate the Trakt client."
    }
  }

  val auth by lazy { TraktAuthApi(client, config) }
  val movies by buildApi(::TraktMoviesApi)
  val shows by buildApi(::TraktShowsApi)
  val seasons by buildApi(::TraktSeasonsApi)
  val episodes by buildApi(::TraktEpisodesApi)
  val checkin by buildApi(::TraktCheckinApi)
  val search by buildApi(::TraktSearchApi)
  val users by buildApi(::TraktUsersApi)
  val sync by buildApi(::TraktSyncApi)
  val recommendations by buildApi(::TraktRecommendationsApi)
  val comments by buildApi(::TraktCommentsApi)

  val showsExtra by buildApi(::TraktShowsApiExtra)

  fun invalidateAuth() {
    // Force Ktor to re-fetch bearer tokens
    // https://youtrack.jetbrains.com/issue/KTOR-4759
    client.pluginOrNull(Auth)
      ?.providers
      ?.filterIsInstance<BearerAuthProvider>()
      ?.firstOrNull()
      ?.clearToken()
  }

  private inline fun <T> buildApi(crossinline builder: (HttpClient) -> T) = lazy { builder(client) }
}

internal object TraktWebConfig {
  const val VERSION = "2"
}

internal object TraktHeader {
  const val API_KEY = "trakt-api-key"
  const val API_VERSION = "trakt-api-version"
}

internal fun TraktClientConfig.applyTiviConfig(
  oauthInfo: TraktOAuthInfo,
  applicationInfo: ApplicationInfo,
  traktAuthRepository: () -> TraktAuthRepository,
) {
  traktApiKey = oauthInfo.clientId
  maxRetries = 3

  logging {
    logger = object : io.ktor.client.plugins.logging.Logger {
      override fun log(message: String) {
        Logger.d("trakt-ktor") { message }
      }
    }
    level = when {
      applicationInfo.debugBuild -> LogLevel.HEADERS
      else -> LogLevel.NONE
    }
  }

  userAuthentication {
    loadTokens {
      traktAuthRepository().getAuthState()
        ?.let { TraktBearerTokens(it.accessToken, it.refreshToken) }
    }

    refreshTokens {
      traktAuthRepository().refreshTokens()
        ?.let { TraktBearerTokens(it.accessToken, it.refreshToken) }
    }
  }
}
