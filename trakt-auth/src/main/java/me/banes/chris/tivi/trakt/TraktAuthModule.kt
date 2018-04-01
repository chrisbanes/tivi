/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.trakt

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import dagger.Module
import dagger.Provides
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.ResponseTypeValues
import javax.inject.Named
import javax.inject.Singleton

@Module
class TraktAuthModule {
    @Singleton
    @Provides
    fun provideAuthConfig(): AuthorizationServiceConfiguration {
        return AuthorizationServiceConfiguration(
                Uri.parse("https://trakt.tv/oauth/authorize"),
                Uri.parse("https://trakt.tv/oauth/token"),
                null)
    }

    @Provides
    fun provideAuthRequest(
        serviceConfig: AuthorizationServiceConfiguration,
        @Named("trakt-client-id") clientId: String
    ): AuthorizationRequest {
        return AuthorizationRequest.Builder(
                serviceConfig,
                clientId,
                ResponseTypeValues.CODE,
                Uri.parse(TraktConstants.URI_AUTH_CALLBACK)).build()
    }

    @Provides
    fun provideAuthService(context: Context): AuthorizationService {
        return AuthorizationService(context)
    }

    @Singleton
    @Provides
    fun provideClientAuth(@Named("trakt-client-secret") clientSecret: String): ClientAuthentication {
        return ClientSecretBasic(clientSecret)
    }

    @Singleton
    @Provides
    @Named("auth")
    fun provideAuthSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("trakt_auth", Context.MODE_PRIVATE)
    }
}