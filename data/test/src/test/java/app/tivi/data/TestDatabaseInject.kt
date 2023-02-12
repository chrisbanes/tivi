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

import android.app.Application
import androidx.room.Room
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.episodes.EpisodeDataSource
import app.tivi.data.episodes.SeasonsEpisodesDataSource
import app.tivi.data.followedshows.TraktFollowedShowsDataSource
import app.tivi.data.showimages.ShowImagesDataSource
import app.tivi.data.shows.ShowDataSource
import app.tivi.inject.ApplicationScope
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Analytics
import app.tivi.util.Logger
import app.tivi.utils.SuccessFakeShowDataSource
import app.tivi.utils.SuccessFakeShowImagesDataSource
import app.tivi.utils.TestTransactionRunner
import app.tivi.utils.TiviTestDatabase
import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.trakt5.TraktV2
import io.mockk.mockk
import me.tatarka.inject.annotations.Provides

abstract class TestDataSourceModule {
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
    fun provideTraktEpisodeDataSource() = traktEpisodeDataSource

    @Provides
    fun provideTmdbEpisodeDataSource() = tmdbEpisodeDataSource

    @Provides
    fun provideSeasonsEpisodesDataSource() = seasonsDataSource

    @Provides
    fun provideTraktShowDataSource(): ShowDataSource = traktShowDataSource

    @Provides
    fun provideTmdbShowDataSource(): ShowDataSource = tmdbShowDataSource

    @Provides
    fun provideTmdbShowImagesDataSource(): ShowImagesDataSource = tmdbShowImagesDataSource
}

interface TestDatabaseModule {
    @Provides
    fun provideTrakt(): TraktV2 = TraktV2("fakefakefake")

    @Provides
    fun provideTmdb(): Tmdb = Tmdb("fakefakefake")

    @Provides
    fun provideTraktAuthState() = TraktAuthState.LOGGED_IN

    @Provides
    fun provideLogger(): Logger = mockk(relaxUnitFun = true)

    @Provides
    fun provideAnalytics(): Analytics = mockk(relaxUnitFun = true)
}

interface TestRoomDatabaseModule {
    @ApplicationScope
    @Provides
    fun provideDatabase(application: Application): TiviRoomDatabase {
        return Room.inMemoryDatabaseBuilder(application, TiviTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @ApplicationScope
    @Provides
    fun provideDatabaseTransactionRunner(): DatabaseTransactionRunner = TestTransactionRunner
}
