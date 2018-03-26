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

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import me.banes.chris.tivi.AppNavigator
import me.banes.chris.tivi.TiviAppNavigator
import me.banes.chris.tivi.TiviApplication
import me.banes.chris.tivi.actions.TiviActions
import me.banes.chris.tivi.actions.TiviActionsImpl
import me.banes.chris.tivi.appinitializers.AndroidJobInitializer
import me.banes.chris.tivi.appinitializers.AppInitializers
import me.banes.chris.tivi.appinitializers.LeakCanaryInitializer
import me.banes.chris.tivi.appinitializers.ThreeTenBpInitializer
import me.banes.chris.tivi.appinitializers.TimberInitializer
import me.banes.chris.tivi.util.AppRxSchedulers
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
    fun provideRxSchedulers(): AppRxSchedulers {
        return AppRxSchedulers()
    }

    @Named("app")
    @Provides
    @Singleton
    fun provideAppPreferences(application: TiviApplication): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }

    @Provides
    @Singleton
    @Named("cache")
    fun provideCacheDir(application: TiviApplication): File {
        return application.cacheDir
    }

    @Provides
    fun provideAppManagers(
        androidJobInitializer: AndroidJobInitializer,
        leakCanaryManager: LeakCanaryInitializer,
        timberManager: TimberInitializer,
        threeTenManager: ThreeTenBpInitializer
    ): AppInitializers {
        return AppInitializers(androidJobInitializer, leakCanaryManager, timberManager, threeTenManager)
    }

    @Provides
    @Singleton
    @Named("app")
    fun provideAppNavigator(context: Context): AppNavigator {
        return TiviAppNavigator(context)
    }

    @Provides
    @Singleton
    fun provideTiviActions(): TiviActions {
        return TiviActionsImpl()
    }
}