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

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.moviebase.tmdb.Tmdb3
import app.tivi.extensions.unsafeLazy
import app.tivi.inject.ApplicationScope
import app.tivi.tmdb.TmdbComponent
import app.tivi.tmdb.TmdbOAuthInfo
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktComponent
import app.tivi.trakt.TraktOAuthInfo
import app.tivi.util.AnalyticsComponent
import app.tivi.util.Logger
import app.tivi.util.LoggerComponent
import app.tivi.util.TiviLogger
import com.uwetrottmann.trakt5.TraktV2
import io.mockk.mockk
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class DatabaseTest {
    @get:Rule(order = 1)
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val component: TestApplicationComponent by unsafeLazy {
        TestApplicationComponent::class.create(ApplicationProvider.getApplicationContext())
    }
}

@Component
@ApplicationScope
abstract class TestApplicationComponent(
    @get:Provides val application: Application,
) : TmdbComponent,
    TraktComponent,
    AnalyticsComponent,
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
    override fun provideTrakt(
        client: OkHttpClient,
        oauthInfo: TraktOAuthInfo,
    ): TraktV2 = TraktV2("fakefakefake")

    @Provides
    override fun provideTmdb(
        client: OkHttpClient,
        tmdbOAuthInfo: TmdbOAuthInfo,
    ): Tmdb3 = Tmdb3("fakefakefake")

    @Provides
    override fun provideLogger(bind: TiviLogger): Logger = mockk(relaxUnitFun = true)
}
