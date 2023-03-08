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

import app.moviebase.tmdb.Tmdb3
import app.tivi.inject.ApplicationScope
import io.ktor.client.engine.okhttp.OkHttp
import me.tatarka.inject.annotations.Provides

interface TmdbComponent {
    @ApplicationScope
    @Provides
    fun provideTmdbImageUrlProvider(tmdbManager: TmdbManager): TmdbImageUrlProvider {
        return tmdbManager.getLatestImageProvider()
    }

    @ApplicationScope
    @Provides
    fun provideTmdb(
        tmdbOAuthInfo: TmdbOAuthInfo,
    ): Tmdb3 = Tmdb3 {
        tmdbApiKey = tmdbOAuthInfo.apiKey

        useTimeout = true
        useCache = true
        maxRetriesOnException = 3

        httpClient(OkHttp) {
            engine {
                // configure here your OkHttp
            }
        }
    }
}
