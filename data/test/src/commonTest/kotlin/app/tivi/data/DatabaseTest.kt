// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
