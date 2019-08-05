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
import app.tivi.inject.ApplicationId
import com.uwetrottmann.trakt5.TraktV2
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.ResponseTypeValues
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
class TraktAuthModule {
    @Provides
    @Singleton
    fun provideTrakt(
        @Named("cache") cacheDir: File,
        interceptor: HttpLoggingInterceptor,
        @Named("trakt-client-id") clientId: String
    ): TraktV2 {
        return object : TraktV2(clientId) {
            override fun setOkHttpClientDefaults(builder: OkHttpClient.Builder) {
                super.setOkHttpClientDefaults(builder)
                builder.apply {
                    addInterceptor(interceptor)
                    cache(Cache(File(cacheDir, "trakt_cache"), 10 * 1024 * 1024))
                }
            }
        }
    }

    @Provides
    fun provideTraktUsersService(traktV2: TraktV2) = traktV2.users()

    @Provides
    fun provideTraktShowsService(traktV2: TraktV2) = traktV2.shows()

    @Provides
    fun provideTraktEpisodesService(traktV2: TraktV2) = traktV2.episodes()

    @Provides
    fun provideTraktSeasonsService(traktV2: TraktV2) = traktV2.seasons()

    @Provides
    fun provideTraktSyncService(traktV2: TraktV2) = traktV2.sync()

    @Provides
    fun provideTraktSearchService(traktV2: TraktV2) = traktV2.search()

    @Singleton
    @Provides
    fun provideAuthConfig(): AuthorizationServiceConfiguration {
        return AuthorizationServiceConfiguration(
                Uri.parse("https://trakt.tv/oauth/authorize"),
                Uri.parse("https://trakt.tv/oauth/token"),
                null)
    }

    @Provides
    fun provideAuthState(traktManager: TraktManager) = runBlocking {
        traktManager.state.first()
    }

    @Provides
    fun provideAuthRequest(
        serviceConfig: AuthorizationServiceConfiguration,
        @Named("trakt-client-id") clientId: String,
        @ApplicationId applicationId: String
    ): AuthorizationRequest {
        return AuthorizationRequest.Builder(
                serviceConfig,
                clientId,
                ResponseTypeValues.CODE,
                Uri.parse("$applicationId://${TraktConstants.URI_AUTH_CALLBACK_PATH}")
        ).build()
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