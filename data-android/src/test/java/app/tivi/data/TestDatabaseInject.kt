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
import app.tivi.data.repositories.episodes.EpisodeDataSource
import app.tivi.data.repositories.episodes.SeasonsEpisodesDataSource
import app.tivi.data.repositories.followedshows.TraktFollowedShowsDataSource
import app.tivi.data.repositories.showimages.ShowImagesDataSource
import app.tivi.data.repositories.shows.ShowDataSource
import app.tivi.inject.Trakt
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import app.tivi.utils.SuccessFakeShowDataSource
import app.tivi.utils.SuccessFakeShowImagesDataSource
import app.tivi.utils.TestTransactionRunner
import app.tivi.utils.TiviTestDatabase
import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.trakt5.TraktV2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.mockk.mockk
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class TestDataSourceModule {
    private val traktFollowedShowsDataSource: TraktFollowedShowsDataSource = mockk()
    private val traktEpisodeDataSource: EpisodeDataSource = mockk()
    private val tmdbEpisodeDataSource: EpisodeDataSource = mockk()
    private val seasonsDataSource: SeasonsEpisodesDataSource = mockk()
    private val traktShowDataSource: ShowDataSource = SuccessFakeShowDataSource
    private val tmdbShowDataSource: ShowDataSource = SuccessFakeShowDataSource
    private val tmdbShowImagesDataSource: ShowImagesDataSource = SuccessFakeShowImagesDataSource

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
}

@InstallIn(SingletonComponent::class)
@Module
object TestDatabaseModule {
    @Provides
    fun provideTrakt(): TraktV2 = TraktV2("fakefakefake")

    @Provides
    fun provideTmdb(): Tmdb = Tmdb("fakefakefake")

    @Provides
    fun provideTraktAuthState() = TraktAuthState.LOGGED_IN

    @Singleton
    @Provides
    fun provideLogger(): Logger = mockk(relaxUnitFun = true)
}

@InstallIn(SingletonComponent::class)
@Module
object TestRoomDatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): TiviDatabase {
        return Room.inMemoryDatabaseBuilder(context, TiviTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Singleton
    @Provides
    fun provideDatabaseTransactionRunner(): DatabaseTransactionRunner = TestTransactionRunner
}
