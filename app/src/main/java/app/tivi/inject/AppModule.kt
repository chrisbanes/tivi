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
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.AppRxSchedulers
import app.tivi.util.Logger
import app.tivi.util.TimberLogger
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.rx2.asCoroutineDispatcher
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Named
import javax.inject.Singleton
import android.text.format.DateFormat as AndroidDateFormat

@Module
class AppModule {
    @Provides
    fun provideContext(application: TiviApplication): Context = application.applicationContext

    @Singleton
    @Provides
    fun provideRxSchedulers(): AppRxSchedulers = AppRxSchedulers(
            io = Schedulers.io(),
            computation = Schedulers.computation(),
            main = AndroidSchedulers.mainThread()
    )

    @Singleton
    @Provides
    fun provideCoroutineDispatchers(schedulers: AppRxSchedulers) = AppCoroutineDispatchers(
            io = schedulers.io.asCoroutineDispatcher(),
            computation = schedulers.computation.asCoroutineDispatcher(),
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
    fun provideLogger(logger: TimberLogger): Logger = logger

    @Singleton
    @Provides
    fun provideDateTimeFormatter(application: TiviApplication): DateTimeFormatter {
        val dateF = AndroidDateFormat.getMediumDateFormat(application) as SimpleDateFormat
        val timeF = AndroidDateFormat.getTimeFormat(application) as SimpleDateFormat
        return DateTimeFormatter.ofPattern("${dateF.toPattern()} ${timeF.toPattern()}")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
    }
}
