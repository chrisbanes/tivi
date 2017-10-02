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
import android.content.SharedPreferences
import android.os.Debug
import android.preference.PreferenceManager
import com.uwetrottmann.tmdb2.Tmdb
import dagger.Module
import dagger.Provides
import me.banes.chris.tivi.BuildConfig
import me.banes.chris.tivi.TiviApplication
import me.banes.chris.tivi.data.PopularDao
import me.banes.chris.tivi.data.TiviDatabase
import me.banes.chris.tivi.data.TiviShowDao
import me.banes.chris.tivi.data.TrendingDao
import me.banes.chris.tivi.data.UserDao
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.DatabaseTxRunner
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    fun provideContext(application: TiviApplication): Context {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun provideTmdb(context: Context): Tmdb {
        return object : Tmdb(BuildConfig.TMDB_API_KEY) {
            override fun setOkHttpClientDefaults(builder: OkHttpClient.Builder) {
                super.setOkHttpClientDefaults(builder)

                builder.apply {
                    val logging = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    }
                    addInterceptor(logging)

                    cache(Cache(File(context.cacheDir, "tmdb_cache"), 10 * 1024 * 1024))
                }
            }
        }
    }

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

    @Singleton
    @Provides
    fun provideDatabaseTransactionRunner(db: TiviDatabase): DatabaseTxRunner {
        return DatabaseTxRunner(db)
    }

    @Singleton
    @Provides
    fun provideRxSchedulers(): AppRxSchedulers {
        return AppRxSchedulers()
    }

    @Named("app")
    @Provides
    @Singleton
    fun provideAppPreferences(application: TiviApplication): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }

}