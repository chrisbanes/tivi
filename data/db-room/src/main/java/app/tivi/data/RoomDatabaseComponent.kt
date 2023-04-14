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
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface RoomDatabaseComponent {
    @ApplicationScope
    @Provides
    fun provideTiviRoomDatabase(
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
    fun provideTiviShowDao(db: TiviRoomDatabase): TiviShowDao = db.showDao()

    @Provides
    fun provideUserDao(db: TiviRoomDatabase): UserDao = db.userDao()

    @Provides
    fun provideTrendingDao(db: TiviRoomDatabase): TrendingDao = db.trendingDao()

    @Provides
    fun providePopularDao(db: TiviRoomDatabase): PopularDao = db.popularDao()

    @Provides
    fun provideWatchedDao(db: TiviRoomDatabase): WatchedShowDao = db.watchedShowsDao()

    @Provides
    fun provideFollowedShowsDao(db: TiviRoomDatabase): FollowedShowsDao = db.followedShowsDao()

    @Provides
    fun provideSeasonsDao(db: TiviRoomDatabase): SeasonsDao = db.seasonsDao()

    @Provides
    fun provideEpisodesDao(db: TiviRoomDatabase): EpisodesDao = db.episodesDao()

    @Provides
    fun provideRelatedShowsDao(db: TiviRoomDatabase): RelatedShowsDao = db.relatedShowsDao()

    @Provides
    fun provideEpisodeWatchesDao(db: TiviRoomDatabase): EpisodeWatchEntryDao = db.episodeWatchesDao()

    @Provides
    fun provideLastRequestsDao(db: TiviRoomDatabase): LastRequestDao = db.lastRequestDao()

    @Provides
    fun provideShowImagesDao(db: TiviRoomDatabase): ShowTmdbImagesDao = db.showImagesDao()

    @Provides
    fun provideShowFtsDao(db: TiviRoomDatabase): ShowFtsDao = db.showFtsDao()

    @Provides
    fun provideRecommendedShowsDao(db: TiviRoomDatabase): RecommendedDao = db.recommendedShowsDao()

    @Provides
    fun provideLibraryShowsDao(db: TiviRoomDatabase): LibraryShowsDao = db.libraryShowsDao()

    @Provides
    fun provideDatabaseTransactionRunner(runner: RoomTransactionRunner): DatabaseTransactionRunner = runner
}
