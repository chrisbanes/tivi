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

import app.moviebase.trakt.Trakt
import app.moviebase.trakt.api.TraktEpisodesApi
import app.moviebase.trakt.api.TraktRecommendationsApi
import app.moviebase.trakt.api.TraktSearchApi
import app.moviebase.trakt.api.TraktSeasonsApi
import app.moviebase.trakt.api.TraktShowsApi
import app.moviebase.trakt.api.TraktSyncApi
import app.moviebase.trakt.api.TraktUsersApi
import app.tivi.inject.ApplicationScope
import app.tivi.trakt.store.AuthStore
import io.ktor.client.engine.okhttp.OkHttp
import me.tatarka.inject.annotations.Provides
import okhttp3.OkHttpClient

interface TraktComponent {

    @ApplicationScope
    @Provides
    fun provideTrakt(
        client: OkHttpClient,
        authStore: AuthStore,
        oauthInfo: TraktOAuthInfo,
    ): Trakt = Trakt {
        traktApiKey = oauthInfo.clientId
        maxRetriesOnException = 3

        userAuthentication {
//            loadBearerTokens {
//                val authState = authStore.get()
//                authState?.accessToken
//                authState?.refreshToken
//                BearerTokens()
//            }
        }

        httpClient(OkHttp) {
            // Probably want to move to using Ktor's caching, timeouts, etc eventually
            engine {
                preconfigured = client
            }
        }
    }

    @Provides
    fun provideTraktUsersService(trakt: Trakt): TraktUsersApi = trakt.users

    @Provides
    fun provideTraktShowsService(trakt: Trakt): TraktShowsApi = trakt.shows

    @Provides
    fun provideTraktEpisodesService(trakt: Trakt): TraktEpisodesApi = trakt.episodes

    @Provides
    fun provideTraktSeasonsService(trakt: Trakt): TraktSeasonsApi = trakt.seasons

    @Provides
    fun provideTraktSyncService(trakt: Trakt): TraktSyncApi = trakt.sync

    @Provides
    fun provideTraktSearchService(trakt: Trakt): TraktSearchApi = trakt.search

    @Provides
    fun provideTraktRecommendationsService(trakt: Trakt): TraktRecommendationsApi = trakt.recommendations
}
