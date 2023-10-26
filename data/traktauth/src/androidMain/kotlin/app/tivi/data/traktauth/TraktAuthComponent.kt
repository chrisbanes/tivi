// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import android.app.Application
import android.content.Context
import android.net.Uri
import app.tivi.appinitializers.AppInitializer
import app.tivi.data.traktauth.store.AuthSharedPreferences
import app.tivi.data.traktauth.store.AuthStore
import app.tivi.data.traktauth.store.TiviAuthStore
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretBasic

actual interface TraktAuthComponent {
  @ApplicationScope
  @Provides
  fun provideAuthConfig(): AuthorizationServiceConfiguration {
    return AuthorizationServiceConfiguration(
      Uri.parse("https://trakt.tv/oauth/authorize"),
      Uri.parse("https://trakt.tv/oauth/token"),
    )
  }

  @ApplicationScope
  @Provides
  fun provideClientAuth(
    info: TraktOAuthInfo,
  ): ClientAuthentication = ClientSecretBasic(info.clientSecret)

  @ApplicationScope
  @Provides
  fun provideAuthSharedPrefs(
    application: Application,
  ): AuthSharedPreferences {
    return application.getSharedPreferences("trakt_auth", Context.MODE_PRIVATE)
  }

  @ApplicationScope
  @Provides
  fun provideAuthService(application: Application): AuthorizationService {
    return AuthorizationService(application)
  }

  @ApplicationScope
  @Provides
  fun provideTraktRefreshTokenAction(impl: AndroidTraktRefreshTokenAction): TraktRefreshTokenAction = impl

  @ApplicationScope
  @Provides
  fun provideTraktLoginAction(impl: AndroidTraktLoginAction): TraktLoginAction = impl

  @IntoSet
  @Provides
  fun provideTraktAuthInitializer(bind: TraktAuthInitializer): AppInitializer = bind

  @ApplicationScope
  @Provides
  fun provideAuthStore(store: TiviAuthStore): AuthStore = store
}
