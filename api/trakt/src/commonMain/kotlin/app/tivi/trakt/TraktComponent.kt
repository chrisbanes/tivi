// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.trakt

import app.moviebase.trakt.api.TraktEpisodesApi
import app.moviebase.trakt.api.TraktRecommendationsApi
import app.moviebase.trakt.api.TraktSearchApi
import app.moviebase.trakt.api.TraktSeasonsApi
import app.moviebase.trakt.api.TraktShowsApi
import app.moviebase.trakt.api.TraktSyncApi
import app.moviebase.trakt.api.TraktUsersApi
import app.tivi.app.ApplicationInfo
import app.tivi.data.traktauth.TraktClient
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

expect interface TraktPlatformComponent

interface TraktComponent : TraktPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideTraktOAuthInfo(
    appInfo: ApplicationInfo,
  ): TraktOAuthInfo = TraktOAuthInfo(
    clientId = when {
      appInfo.debugBuild -> {
        BuildConfig.TRAKT_DEBUG_CLIENT_ID.ifEmpty { BuildConfig.TRAKT_CLIENT_ID }
      }

      else -> BuildConfig.TRAKT_CLIENT_ID
    },
    clientSecret = when {
      appInfo.debugBuild -> {
        BuildConfig.TRAKT_DEBUG_CLIENT_SECRET
          .ifEmpty { BuildConfig.TRAKT_CLIENT_SECRET }
      }

      else -> BuildConfig.TRAKT_CLIENT_SECRET
    },
    redirectUri = "${appInfo.packageName}://auth/oauth2callback",
  )

  @Provides
  fun provideTraktUsersService(trakt: TiviTrakt): TraktUsersApi = trakt.users

  @Provides
  fun provideTraktShowsService(trakt: TiviTrakt): TraktShowsApi = trakt.shows

  @Provides
  fun provideTraktShowsExtraService(trakt: TiviTrakt): TraktShowsApiExtra = trakt.showsExtra

  @Provides
  fun provideTraktEpisodesService(trakt: TiviTrakt): TraktEpisodesApi = trakt.episodes

  @Provides
  fun provideTraktSeasonsService(trakt: TiviTrakt): TraktSeasonsApi = trakt.seasons

  @Provides
  fun provideTraktSyncService(trakt: TiviTrakt): TraktSyncApi = trakt.sync

  @Provides
  fun provideTraktSearchService(trakt: TiviTrakt): TraktSearchApi = trakt.search

  @Provides
  fun provideTraktRecommendationsService(trakt: TiviTrakt): TraktRecommendationsApi = trakt.recommendations

  @Provides
  fun provideTraktClient(trakt: Lazy<TiviTrakt>): TraktClient = object : TraktClient {
    override fun invalidateAuthTokens() {
      trakt.value.invalidateAuth()
    }
  }
}
