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

package app.tivi.trakt

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import app.tivi.app.ApplicationInfo
import app.tivi.inject.ApplicationScope
import app.tivi.trakt.store.AuthSharedPreferences
import app.tivi.trakt.store.AuthStore
import app.tivi.trakt.store.TiviAuthStore
import me.tatarka.inject.annotations.Provides
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.ResponseTypeValues

interface TraktAuthModule {
    @ApplicationScope
    @Provides
    fun provideAuthConfig(): AuthorizationServiceConfiguration {
        return AuthorizationServiceConfiguration(
            Uri.parse("https://trakt.tv/oauth/authorize"),
            Uri.parse("https://trakt.tv/oauth/token"),
        )
    }

    @Provides
    fun provideAuthRequest(
        serviceConfig: AuthorizationServiceConfiguration,
        oauthInfo: TraktOAuthInfo,
        appInfo: ApplicationInfo,
    ): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfig,
            oauthInfo.clientId,
            ResponseTypeValues.CODE,
            oauthInfo.redirectUri.toUri(),
        ).apply {
            // Disable PKCE since Trakt does not support it
            setCodeVerifier(null)
        }.build()
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
    fun provideTraktAuthManager(manager: ActivityTraktAuthManager): TraktAuthManager = manager

    @ApplicationScope
    @Provides
    fun provideAuthStore(manager: TiviAuthStore): AuthStore = manager
}
