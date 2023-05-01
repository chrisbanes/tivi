/*
 * Copyright 2020 Google LLC
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

package app.tivi.data

import app.moviebase.tmdb.Tmdb3
import app.moviebase.trakt.Trakt
import app.tivi.data.traktauth.RefreshTraktTokensInteractor
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.data.traktauth.store.AuthStore
import app.tivi.extensions.unsafeLazy
import app.tivi.inject.ApplicationScope
import app.tivi.tmdb.TmdbComponent
import app.tivi.tmdb.TmdbOAuthInfo
import app.tivi.trakt.TraktComponent
import app.tivi.util.LoggerComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import okhttp3.OkHttpClient

abstract class DatabaseTest {
    val component: TestApplicationComponent by unsafeLazy {
        TestApplicationComponent::class.create()
    }
}

@Component
@ApplicationScope
abstract class TestApplicationComponent :
    TmdbComponent,
    TraktComponent,
    LoggerComponent,
    TestDataSourceComponent(),
    TestRoomDatabaseComponent {

    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    fun provideTraktOAuthInfo(): TraktOAuthInfo = TraktOAuthInfo(
        clientId = "",
        clientSecret = "",
        redirectUri = "",
    )

    @Provides
    fun provideTmdbOAuthInfo(): TmdbOAuthInfo = TmdbOAuthInfo(
        apiKey = "",
    )

    @Provides
    fun provideTraktAuthState(): TraktAuthState = TraktAuthState.LOGGED_IN

    @Provides
    fun provideRefreshTraktTokensInteractor(): RefreshTraktTokensInteractor {
        return RefreshTraktTokensInteractor { null }
    }

    @Provides
    override fun provideTrakt(
        client: OkHttpClient,
        authStore: AuthStore,
        oauthInfo: TraktOAuthInfo,
        refreshTokens: Lazy<RefreshTraktTokensInteractor>,
    ): Trakt = Trakt("fakefakefake")

    @Provides
    override fun provideTmdb(
        client: OkHttpClient,
        tmdbOAuthInfo: TmdbOAuthInfo,
    ): Tmdb3 = Tmdb3("fakefakefake")
}
