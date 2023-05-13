/*
 * Copyright 2017 Google LLC
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

package app.tivi.data.traktauth

import android.app.Application
import android.content.Context
import android.net.Uri
import app.tivi.data.traktauth.store.AuthSharedPreferences
import app.tivi.data.traktauth.store.AuthStore
import app.tivi.data.traktauth.store.TiviAuthStore
import app.tivi.inject.ActivityScope
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretBasic

interface TraktAuthComponent {
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
    fun provideRefreshTraktTokensInteractor(impl: AndroidRefreshTraktTokensInteractor): RefreshTraktTokensInteractor = impl

    @ApplicationScope
    @Provides
    fun provideAuthStore(manager: TiviAuthStore): AuthStore = manager
}

interface TraktAuthActivityComponent {
    @ActivityScope
    @Provides
    fun provideLoginToTraktInteractor(impl: AndroidLoginToTraktInteractor): LoginToTraktInteractor = impl
}
