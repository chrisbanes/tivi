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

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import app.tivi.inject.ApplicationId
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.ResponseTypeValues
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TraktAuthModule {
    @Singleton
    @Provides
    fun provideAuthConfig(): AuthorizationServiceConfiguration {
        return AuthorizationServiceConfiguration(
            Uri.parse("https://trakt.tv/oauth/authorize"),
            Uri.parse("https://trakt.tv/oauth/token")
        )
    }

    @Provides
    fun provideAuthState(traktManager: TraktManager) = runBlocking {
        traktManager.state.first()
    }

    @Provides
    fun provideAuthRequest(
        serviceConfig: AuthorizationServiceConfiguration,
        @Named("trakt-client-id") clientId: String,
        @Named("trakt-auth-redirect-uri") redirectUri: String
    ): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri.toUri()
        ).apply {
            // Disable PKCE since Trakt does not support it
            setCodeVerifier(null)
        }.build()
    }

    @Singleton
    @Named("trakt-auth-redirect-uri")
    @Provides
    fun provideAuthRedirectUri(
        @ApplicationId applicationId: String
    ): String = "$applicationId://${TraktConstants.URI_AUTH_CALLBACK_PATH}"

    @Singleton
    @Provides
    fun provideClientAuth(
        @Named("trakt-client-secret") clientSecret: String
    ): ClientAuthentication {
        return ClientSecretBasic(clientSecret)
    }

    @Singleton
    @Provides
    @Named("auth")
    fun provideAuthSharedPrefs(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("trakt_auth", Context.MODE_PRIVATE)
    }
}
