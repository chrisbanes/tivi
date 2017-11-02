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
import com.uwetrottmann.trakt5.TraktV2
import dagger.Module
import dagger.Provides
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
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
class TraktModule {

    @Singleton
    @Provides
    fun provideAuthConfig(): AuthorizationServiceConfiguration {
        return AuthorizationServiceConfiguration(
                Uri.parse("https://trakt.tv/oauth/authorize"),
                Uri.parse("https://trakt.tv/oauth/token"),
                null)
    }

    @Provides
    fun provideAuthRequest(serviceConfig: AuthorizationServiceConfiguration): AuthorizationRequest {
        return AuthorizationRequest.Builder(
                serviceConfig,
                BuildConfig.TRAKT_CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(TraktConstants.URI_AUTH_CALLBACK)).build()
    }

    @Provides
    fun provideAuthService(context: Context): AuthorizationService {
        return AuthorizationService(context)
    }

    @Singleton
    @Provides
    fun provideClientAuth(): ClientAuthentication {
        return ClientSecretBasic(BuildConfig.TRAKT_CLIENT_SECRET)
    }

    @Singleton
    @Provides
    @Named("auth")
    fun provideAuthSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("trakt_auth", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideTrakt(@Named("cache") cacheDir: File, interceptor: HttpLoggingInterceptor): TraktV2 {
        return object : TraktV2(BuildConfig.TRAKT_CLIENT_ID) {
            override fun setOkHttpClientDefaults(builder: OkHttpClient.Builder) {
                super.setOkHttpClientDefaults(builder)
                builder.apply {
                    addInterceptor(interceptor)
                    cache(Cache(File(cacheDir, "trakt_cache"), 10 * 1024 * 1024))
                }
            }
        }
    }

}