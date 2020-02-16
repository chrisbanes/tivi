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

package app.tivi.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.tivi.data.dao.EpisodeWatchEntryTest
import app.tivi.data.dao.EpisodesTest
import app.tivi.data.dao.SeasonsTest
import app.tivi.data.repositories.FollowedShowRepositoryTest
import app.tivi.data.repositories.SeasonsEpisodesRepositoryTest
import app.tivi.data.repositories.episodes.EpisodeDataSource
import app.tivi.data.repositories.episodes.SeasonsEpisodesDataSource
import app.tivi.data.repositories.followedshows.TraktFollowedShowsDataSource
import app.tivi.data.repositories.showimages.ShowImagesDataSource
import app.tivi.data.repositories.shows.ShowDataSource
import app.tivi.inject.ForStore
import app.tivi.inject.Trakt
import app.tivi.trakt.TraktAuthState
import app.tivi.trakt.TraktServiceModule
import app.tivi.util.Logger
import app.tivi.utils.TestTransactionRunner
import app.tivi.utils.TiviTestDatabase
import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.trakt5.TraktV2
import dagger.Component
import dagger.Module
import dagger.Provides
import io.mockk.mockk
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Singleton
@Component(modules = [
    TestDataSourceModule::class,
    TestDatabaseModule::class
])
interface TestComponent {
    fun inject(test: SeasonsEpisodesRepositoryTest)
    fun inject(test: FollowedShowRepositoryTest)
    fun inject(test: EpisodesTest)
    fun inject(test: EpisodeWatchEntryTest)
    fun inject(test: SeasonsTest)
}

@Module
class TestDataSourceModule(
    private val traktFollowedShowsDataSource: TraktFollowedShowsDataSource = mockk(),
    private val traktEpisodeDataSource: EpisodeDataSource = mockk(),
    private val tmdbEpisodeDataSource: EpisodeDataSource = mockk(),
    private val seasonsDataSource: SeasonsEpisodesDataSource = mockk(),
    private val traktShowDataSource: ShowDataSource = mockk(),
    private val tmdbShowDataSource: ShowDataSource = mockk(),
    private val tmdbShowImagesDataSource: ShowImagesDataSource = mockk(),
    private val storeScope: CoroutineScope
) {
    @Provides
    fun provideTraktFollowedShowsDataSource() = traktFollowedShowsDataSource

    @Provides
    @Trakt
    fun provideTraktEpisodeDataSource() = traktEpisodeDataSource

    @Provides
    @app.tivi.inject.Tmdb
    fun provideTmdbEpisodeDataSource() = tmdbEpisodeDataSource

    @Provides
    fun provideSeasonsEpisodesDataSource() = seasonsDataSource

    @Provides
    @Trakt
    fun provideTraktShowDataSource(): ShowDataSource = traktShowDataSource

    @Provides
    @app.tivi.inject.Tmdb
    fun provideTmdbShowDataSource(): ShowDataSource = tmdbShowDataSource

    @Provides
    @app.tivi.inject.Tmdb
    fun provideTmdbShowImagesDataSource(): ShowImagesDataSource = tmdbShowImagesDataSource

    @Provides
    @ForStore
    fun provideStoreCoroutineScope(): CoroutineScope = storeScope
}

@Module(includes = [
    TestRoomDatabaseModule::class,
    DatabaseDaoModule::class,
    TraktServiceModule::class,
    ShowStoreModule::class
])
class TestDatabaseModule {
    @Provides
    fun provideTrakt(): TraktV2 = TraktV2("fakefakefake")

    @Provides
    fun provideTmdb(): Tmdb = Tmdb("fakefakefake")

    @Provides
    fun provideTraktAuthState() = TraktAuthState.LOGGED_IN

    @Provides
    fun provideContext(): Context = ApplicationProvider.getApplicationContext()

    @Singleton
    @Provides
    fun provideLogger(): Logger = mockk(relaxUnitFun = true)
}

@Module
class TestRoomDatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(context: Context): TiviDatabase {
        return Room.inMemoryDatabaseBuilder(context, TiviTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Singleton
    @Provides
    fun provideDatabaseTransactionRunner(): DatabaseTransactionRunner = TestTransactionRunner
}
