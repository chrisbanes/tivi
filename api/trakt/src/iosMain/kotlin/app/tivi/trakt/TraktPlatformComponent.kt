// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.trakt

import app.tivi.app.ApplicationInfo
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.inject.ApplicationScope
import io.ktor.client.engine.darwin.Darwin
import me.tatarka.inject.annotations.Provides

actual interface TraktPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideTrakt(
    oauthInfo: TraktOAuthInfo,
    applicationInfo: ApplicationInfo,
    traktAuthRepository: Lazy<TraktAuthRepository>,
  ): TiviTrakt = TiviTrakt {
    applyTiviConfig(
      oauthInfo = oauthInfo,
      applicationInfo = applicationInfo,
      traktAuthRepository = traktAuthRepository::value,
    )

    httpClient(Darwin) {
      engine {
        configureRequest {
          setAllowsCellularAccess(true)
        }
      }
    }
  }
}
