// Copyright 2019, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.trakt

import app.tivi.app.ApplicationInfo
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.inject.ApplicationScope
import io.ktor.client.engine.okhttp.OkHttp
import me.tatarka.inject.annotations.Provides
import okhttp3.OkHttpClient

actual interface TraktPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideTrakt(
    client: OkHttpClient,
    oauthInfo: TraktOAuthInfo,
    applicationInfo: ApplicationInfo,
    traktAuthRepository: Lazy<TraktAuthRepository>,
  ): TiviTrakt = TiviTrakt {
    applyTiviConfig(
      oauthInfo = oauthInfo,
      applicationInfo = applicationInfo,
      traktAuthRepository = traktAuthRepository::value,
    )

    httpClient(OkHttp) {
      // Probably want to move to using Ktor's caching, timeouts, etc eventually
      engine {
        preconfigured = client
      }
    }
  }
}
