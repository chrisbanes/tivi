// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tmdb

import app.moviebase.tmdb.Tmdb3
import app.tivi.inject.ApplicationScope
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.http.HttpStatusCode
import me.tatarka.inject.annotations.Provides
import okhttp3.OkHttpClient

actual interface TmdbPlatformComponent {
    @ApplicationScope
    @Provides
    fun provideTmdb(
        client: OkHttpClient,
        tmdbOAuthInfo: TmdbOAuthInfo,
    ): Tmdb3 = Tmdb3 {
        tmdbApiKey = tmdbOAuthInfo.apiKey
        maxRetriesOnException = 3

        httpClient(OkHttp) {
            // Probably want to move to using Ktor's caching, timeouts, etc eventually
            engine {
                preconfigured = client
            }

            install(HttpRequestRetry) {
                retryIf(5) { _, httpResponse ->
                    when {
                        httpResponse.status.value in 500..599 -> true
                        httpResponse.status == HttpStatusCode.TooManyRequests -> true
                        else -> false
                    }
                }
            }
        }
    }
}
