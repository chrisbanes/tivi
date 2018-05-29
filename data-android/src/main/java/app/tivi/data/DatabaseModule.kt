/*
 * Copyright 2017 Google, Inc.
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

import android.arch.persistence.room.Room
import android.content.Context
import android.os.Debug
import dagger.Module
import dagger.Provides
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.PopularDao
import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.TrendingDao
import app.tivi.data.daos.UserDao
import app.tivi.data.daos.WatchedShowDao
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(context: Context): TiviDatabase {
        val builder = Room.databaseBuilder(context, TiviDatabase::class.java, "shows.db")
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

    @Singleton
    @Provides
    fun provideDatabaseTransactionRunner(db: TiviDatabase): DatabaseTransactionRunner = RoomTransactionRunner(db)
}