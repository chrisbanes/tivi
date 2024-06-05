// Copyright 2017, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tmdb

import app.tivi.app.ApplicationInfo
import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface TmdbComponent :
  TmdbCommonComponent,
  TmdbPlatformComponent

expect interface TmdbPlatformComponent

interface TmdbCommonComponent {
  @ApplicationScope
  @Provides
  fun provideTmdbApiKey(
    appInfo: ApplicationInfo,
  ): TmdbOAuthInfo = TmdbOAuthInfo(
    apiKey = when {
      appInfo.debugBuild -> {
        BuildConfig.TMDB_DEBUG_API_KEY.ifEmpty { BuildConfig.TMDB_API_KEY }
      }

      else -> BuildConfig.TMDB_API_KEY
    },
  )

  @ApplicationScope
  @Provides
  fun provideTmdbImageUrlProvider(tmdbManager: TmdbManager): TmdbImageUrlProvider {
    return tmdbManager.getLatestImageProvider()
  }

  @Provides
  @IntoSet
  fun provideTmdbInitializer(bind: TmdbInitializer): AppInitializer = bind
}
