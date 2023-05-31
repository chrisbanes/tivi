// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tmdb

import app.moviebase.tmdb.Tmdb3
import app.tivi.inject.ApplicationScope
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.http.HttpStatusCode
import me.tatarka.inject.annotations.Provides

actual interface TmdbPlatformComponent {
    @ApplicationScope
    @Provides
    fun provideTmdb(
        tmdbOAuthInfo: TmdbOAuthInfo,
    ): Tmdb3 = Tmdb3 {
        tmdbApiKey = tmdbOAuthInfo.apiKey
        maxRetriesOnException = 3

        httpClient(Darwin) {
            engine {
                configureRequest {
                    setAllowsCellularAccess(true)
                }
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
