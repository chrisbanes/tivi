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

package me.banes.chris.tivi.inject

import android.arch.persistence.room.Room
import android.content.Context
import android.os.Debug
import dagger.Module
import dagger.Provides
import me.banes.chris.tivi.data.TiviDatabase
import me.banes.chris.tivi.data.daos.PopularDao
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.daos.TrendingDao
import me.banes.chris.tivi.data.daos.UserDao
import me.banes.chris.tivi.data.daos.WatchedDao
import me.banes.chris.tivi.util.DatabaseTxRunner
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
    fun provideTiviShowDao(db: TiviDatabase): TiviShowDao {
        return db.showDao()
    }

    @Provides
    fun provideUserDao(db: TiviDatabase): UserDao {
        return db.userDao()
    }

    @Provides
    fun provideTrendingDao(db: TiviDatabase): TrendingDao {
        return db.trendingDao()
    }

    @Provides
    fun providePopularDao(db: TiviDatabase): PopularDao {
        return db.popularDao()
    }

    @Provides
    fun provideWatchedDao(db: TiviDatabase): WatchedDao {
        return db.watchedDao()
    }

    @Singleton
    @Provides
    fun provideDatabaseTransactionRunner(db: TiviDatabase): DatabaseTxRunner {
        return DatabaseTxRunner(db)
    }

}