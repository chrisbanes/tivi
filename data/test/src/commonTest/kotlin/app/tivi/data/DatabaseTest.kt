// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

import app.cash.sqldelight.db.SqlDriver
import app.moviebase.tmdb.Tmdb3
import app.moviebase.trakt.Trakt
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.data.traktauth.TraktLoginAction
import app.tivi.data.traktauth.TraktRefreshTokenAction
import app.tivi.inject.ApplicationCoroutineScope
import app.tivi.inject.ApplicationScope
import app.tivi.tmdb.TmdbCommonComponent
import app.tivi.trakt.TraktCommonComponent
import app.tivi.util.Logger
import app.tivi.utils.SuccessRefreshTokenAction
import app.tivi.utils.SuccessTraktLoginAction
import com.benasher44.uuid.uuid4
import kotlin.test.AfterTest
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

abstract class DatabaseTest {
  val applicationComponent = TestApplicationComponent::class.create()

  @AfterTest
  fun closeDatabase() {
    applicationComponent.sqlDriver.close()
  }
}

@Component
@ApplicationScope
abstract class TestApplicationComponent :
  TmdbCommonComponent,
  TraktCommonComponent,
  TestDataSourceComponent(),
  TestDatabaseComponent {

  @Provides
  fun provideTraktAuthState(): TraktAuthState = TraktAuthState.LOGGED_IN

  @Provides
  fun provideRefreshTraktTokensInteractor(): TraktRefreshTokenAction = SuccessRefreshTokenAction

  @Provides
  fun provideTraktLoginAction(): TraktLoginAction = SuccessTraktLoginAction

  @Provides
  fun provideTrakt(): Trakt = Trakt("fakefakefake")

  @Provides
  fun provideTmdb(): Tmdb3 = Tmdb3("fakefakefake")

  @Provides
  override fun provideDatabaseConfiguration(): DatabaseConfiguration {
    return DatabaseConfiguration(name = uuid4().toString(), inMemory = true)
  }

  @Provides
  fun provideLogger(): Logger = object : Logger {}

  @OptIn(DelicateCoroutinesApi::class)
  @Provides
  fun provideCoroutineScope(): ApplicationCoroutineScope = GlobalScope

  abstract val sqlDriver: SqlDriver
}
