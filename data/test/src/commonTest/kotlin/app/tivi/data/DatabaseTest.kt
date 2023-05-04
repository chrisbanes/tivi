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
import app.tivi.extensions.unsafeLazy
import app.tivi.inject.ApplicationScope
import app.tivi.tmdb.TmdbCommonComponent
import app.tivi.trakt.TraktCommonComponent
import app.tivi.util.LoggerComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

abstract class DatabaseTest {
    val component: TestApplicationComponent by unsafeLazy {
        TestApplicationComponent::class.create()
    }
}

@Component
@ApplicationScope
abstract class TestApplicationComponent :
    TmdbCommonComponent,
    TraktCommonComponent,
    LoggerComponent,
    TestDataSourceComponent(),
    TestDatabaseComponent {

    @Provides
    fun provideTraktAuthState(): TraktAuthState = TraktAuthState.LOGGED_IN

    @Provides
    fun provideRefreshTraktTokensInteractor(): RefreshTraktTokensInteractor {
        return RefreshTraktTokensInteractor { null }
    }

    @Provides
    fun provideTrakt(): Trakt = Trakt("fakefakefake")

    @Provides
    fun provideTmdb(): Tmdb3 = Tmdb3("fakefakefake")
}
