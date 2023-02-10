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
import android.os.Debug
import androidx.room.Room
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.LibraryShowsDao
import app.tivi.data.daos.PopularDao
import app.tivi.data.daos.RecommendedDao
import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.ShowFtsDao
import app.tivi.data.daos.ShowTmdbImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.TrendingDao
import app.tivi.data.daos.UserDao
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.db.RoomTransactionRunner
import app.tivi.data.db.TiviDatabase
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ApplicationScope
abstract class RoomDatabaseModule {
    @Provides
    fun provideDatabase(
        application: Application,
    ): TiviRoomDatabase {
        val builder = Room.databaseBuilder(application, TiviRoomDatabase::class.java, "shows.db")
            .fallbackToDestructiveMigration()
        if (Debug.isDebuggerConnected()) {
            builder.allowMainThreadQueries()
        }
        return builder.build()
    }

    @Provides
    fun provideTiviShowDao(db: TiviDatabase): TiviShowDao = db.showDao()

    @Provides
    fun provideUserDao(db: TiviDatabase): UserDao = db.userDao()

    @Provides
    fun provideTrendingDao(db: TiviDatabase): TrendingDao = db.trendingDao()

    @Provides
    fun providePopularDao(db: TiviDatabase): PopularDao = db.popularDao()

    @Provides
    fun provideWatchedDao(db: TiviDatabase): WatchedShowDao = db.watchedShowsDao()

    @Provides
    fun provideFollowedShowsDao(db: TiviDatabase): FollowedShowsDao = db.followedShowsDao()

    @Provides
    fun provideSeasonsDao(db: TiviDatabase): SeasonsDao = db.seasonsDao()

    @Provides
    fun provideEpisodesDao(db: TiviDatabase): EpisodesDao = db.episodesDao()

    @Provides
    fun provideRelatedShowsDao(db: TiviDatabase): RelatedShowsDao = db.relatedShowsDao()

    @Provides
    fun provideEpisodeWatchesDao(db: TiviDatabase): EpisodeWatchEntryDao = db.episodeWatchesDao()

    @Provides
    fun provideLastRequestsDao(db: TiviDatabase): LastRequestDao = db.lastRequestDao()

    @Provides
    fun provideShowImagesDao(db: TiviDatabase): ShowTmdbImagesDao = db.showImagesDao()

    @Provides
    fun provideShowFtsDao(db: TiviDatabase): ShowFtsDao = db.showFtsDao()

    @Provides
    fun provideRecommendedShowsDao(db: TiviDatabase): RecommendedDao = db.recommendedShowsDao()

    @Provides
    fun provideLibraryShowsDao(db: TiviDatabase): LibraryShowsDao = db.libraryShowsDao()

    @Provides
    fun bindTiviDatabase(db: TiviRoomDatabase): TiviDatabase = db

    @Provides
    @ApplicationScope
    fun provideDatabaseTransactionRunner(runner: RoomTransactionRunner): DatabaseTransactionRunner = runner
}
