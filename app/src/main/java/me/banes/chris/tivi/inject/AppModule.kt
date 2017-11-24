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
import me.banes.chris.tivi.appmanagers.AppManagers
import me.banes.chris.tivi.appmanagers.EmojiCompatManager
import me.banes.chris.tivi.appmanagers.LeakCanaryManager
import me.banes.chris.tivi.appmanagers.ThreeTenBpManager
import me.banes.chris.tivi.appmanagers.TimberManager
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
            leakCanaryManager: LeakCanaryManager,
            timberManager: TimberManager,
            threeTenManager: ThreeTenBpManager,
            emojiCompatManager: EmojiCompatManager): AppManagers {
        return AppManagers(leakCanaryManager, timberManager, threeTenManager, emojiCompatManager)
    }

    @Provides
    @Singleton
    fun provideAppNavigator(context: Context): AppNavigator {
        return TiviAppNavigator(context)
    }
}