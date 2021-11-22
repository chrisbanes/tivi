/*
 * Copyright 2019 Google LLC
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

import com.uwetrottmann.trakt5.TraktV2
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [TraktServiceModule::class])
object TraktModule {
    @Provides
    @Singleton
    fun provideTrakt(
        client: OkHttpClient,
        @Named("trakt-client-id") clientId: String,
        @Named("trakt-client-secret") clientSecret: String,
        @Named("trakt-auth-redirect-uri") redirectUri: String,
        @Named("chucker") chucker: Interceptor,
    ): TraktV2 = object : TraktV2(clientId, clientSecret, redirectUri) {
        override fun okHttpClient(): OkHttpClient {
            return client.newBuilder()
                .also { setOkHttpClientDefaults(it) }
                .addInterceptor(chucker)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
        }
    }
}

@Module
object TraktServiceModule {
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

    @Provides
    fun provideTraktRecommendationsService(traktV2: TraktV2) = traktV2.recommendations()
}
