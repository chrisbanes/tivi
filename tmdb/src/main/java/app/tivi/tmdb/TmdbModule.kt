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

package app.tivi.tmdb

import com.uwetrottmann.tmdb2.Tmdb
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
object TmdbModule {
    @Provides
    fun provideTmdbImageUrlProvider(tmdbManager: TmdbManager): TmdbImageUrlProvider {
        return tmdbManager.getLatestImageProvider()
    }

    @Singleton
    @Provides
    fun provideTmdb(
        client: OkHttpClient,
        @Named("tmdb-api") apiKey: String,
        @Named("chucker") chucker: Interceptor,
    ): Tmdb {
        return object : Tmdb(apiKey) {
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
}
