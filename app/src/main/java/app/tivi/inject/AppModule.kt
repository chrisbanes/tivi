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

package app.tivi.inject

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import app.tivi.AppNavigator
import app.tivi.BuildConfig
import app.tivi.TiviAppNavigator
import app.tivi.TiviApplication
import app.tivi.actions.ShowTasks
import app.tivi.appinitializers.AndroidJobInitializer
import app.tivi.appinitializers.AppInitializers
import app.tivi.appinitializers.ThreeTenBpInitializer
import app.tivi.appinitializers.TimberInitializer
import app.tivi.tasks.ShowTasksImpl
import app.tivi.util.AndroidLogger
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.AppRxSchedulers
import app.tivi.util.Logger
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.rx2.asCoroutineDispatcher
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule {
    @Provides
    fun provideContext(application: TiviApplication): Context = application.applicationContext

    @Singleton
    @Provides
    fun provideRxSchedulers(): AppRxSchedulers = AppRxSchedulers(
            database = Schedulers.single(),
            disk = Schedulers.io(),
            network = Schedulers.io(),
            main = AndroidSchedulers.mainThread()
    )

    @Singleton
    @Provides
    fun provideCoroutineDispatchers(schedulers: AppRxSchedulers) = AppCoroutineDispatchers(
            database = schedulers.database.asCoroutineDispatcher(),
            disk = schedulers.disk.asCoroutineDispatcher(),
            network = schedulers.network.asCoroutineDispatcher(),
            main = UI
    )

    @Named("app")
    @Provides
    @Singleton
    fun provideAppPreferences(application: TiviApplication): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }

    @Provides
    @Singleton
    @Named("cache")
    fun provideCacheDir(application: TiviApplication): File = application.cacheDir

    @Provides
    fun provideAppManagers(
        androidJobInitializer: AndroidJobInitializer,
        timberManager: TimberInitializer,
        threeTenManager: ThreeTenBpInitializer
    ) = AppInitializers(androidJobInitializer, timberManager, threeTenManager)

    @Provides
    @Singleton
    @Named("app")
    fun provideAppNavigator(context: Context): AppNavigator = TiviAppNavigator(context)

    @Provides
    @Singleton
    fun provideTiviActions(): ShowTasks = ShowTasksImpl()

    @Provides
    @Named("tmdb-api")
    fun provideTmdbApiKey(): String = BuildConfig.TMDB_API_KEY

    @Provides
    @Named("trakt-client-id")
    fun provideTraktClientId(): String = BuildConfig.TRAKT_CLIENT_ID

    @Provides
    @Named("trakt-client-secret")
    fun provideTraktClientSecret(): String = BuildConfig.TRAKT_CLIENT_SECRET

    @Singleton
    @Provides
    @ApplicationLevel
    fun provideCompositeDisposable() = CompositeDisposable()

    @Singleton
    @Provides
    fun provideLogger(): Logger = AndroidLogger
}
