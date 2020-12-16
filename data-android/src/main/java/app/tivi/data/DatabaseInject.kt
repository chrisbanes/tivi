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
import android.os.Debug
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RoomDatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TiviRoomDatabase {
        val builder = Room.databaseBuilder(context, TiviRoomDatabase::class.java, "shows.db")
            .addMigrations(*TiviRoomDatabase_Migrations.build())
            .fallbackToDestructiveMigration()
        if (Debug.isDebuggerConnected()) {
            builder.allowMainThreadQueries()
        }
        return builder.build()
    }
}

@InstallIn(SingletonComponent::class)
@Module
object DatabaseDaoModule {
    @Provides
    fun provideTiviShowDao(db: TiviDatabase) = db.showDao()

    @Provides
    fun provideUserDao(db: TiviDatabase) = db.userDao()

    @Provides
    fun provideTrendingDao(db: TiviDatabase) = db.trendingDao()

    @Provides
    fun providePopularDao(db: TiviDatabase) = db.popularDao()

    @Provides
    fun provideWatchedDao(db: TiviDatabase) = db.watchedShowsDao()

    @Provides
    fun provideFollowedShowsDao(db: TiviDatabase) = db.followedShowsDao()

    @Provides
    fun provideSeasonsDao(db: TiviDatabase) = db.seasonsDao()

    @Provides
    fun provideEpisodesDao(db: TiviDatabase) = db.episodesDao()

    @Provides
    fun provideRelatedShowsDao(db: TiviDatabase) = db.relatedShowsDao()

    @Provides
    fun provideEpisodeWatchesDao(db: TiviDatabase) = db.episodeWatchesDao()

    @Provides
    fun provideLastRequestsDao(db: TiviDatabase) = db.lastRequestDao()

    @Provides
    fun provideShowImagesDao(db: TiviDatabase) = db.showImagesDao()

    @Provides
    fun provideShowFtsDao(db: TiviDatabase) = db.showFtsDao()

    @Provides
    fun provideRecommendedShowsDao(db: TiviDatabase) = db.recommendedShowsDao()
}

@InstallIn(SingletonComponent::class)
@Module
abstract class DatabaseModuleBinds {
    @Binds
    abstract fun bindTiviDatabase(db: TiviRoomDatabase): TiviDatabase

    @Singleton
    @Binds
    abstract fun provideDatabaseTransactionRunner(runner: RoomTransactionRunner): DatabaseTransactionRunner
}
