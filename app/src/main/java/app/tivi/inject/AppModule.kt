/*
 * Copyright 2017 Google LLC
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
import android.os.Build
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.navigation.ui.AppBarConfiguration
import androidx.preference.PreferenceManager
import app.tivi.BuildConfig
import app.tivi.TiviApplication
import app.tivi.home.followed.R
import app.tivi.util.AppCoroutineDispatchers
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton
import android.text.format.DateFormat as AndroidDateFormat

@Module(includes = [AppModuleBinds::class])
class AppModule {
    @Provides
    fun provideContext(application: TiviApplication): Context = application.applicationContext

    @ApplicationId
    @Provides
    fun provideApplicationId(application: TiviApplication): String = application.packageName

    @Singleton
    @Provides
    fun provideCoroutineDispatchers() = AppCoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main
    )

    @Singleton
    @Provides
    fun provideBackgroundExecutor(): Executor {
        val parallelism = (Runtime.getRuntime().availableProcessors() * 2)
                .coerceIn(4, 32)
        return if (Build.VERSION.SDK_INT < 24) {
            Executors.newFixedThreadPool(parallelism)
        } else {
            Executors.newWorkStealingPool(parallelism)
        }
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
    fun provideCacheDir(application: TiviApplication): File = application.cacheDir

    @Provides
    @Named("tmdb-api")
    fun provideTmdbApiKey(): String = BuildConfig.TMDB_API_KEY

    @Provides
    @Named("trakt-client-id")
    fun provideTraktClientId(): String = BuildConfig.TRAKT_CLIENT_ID

    @Provides
    @Named("trakt-client-secret")
    fun provideTraktClientSecret(): String = BuildConfig.TRAKT_CLIENT_SECRET

    @Provides
    fun provideCompositeDisposable() = CompositeDisposable()

    @Singleton
    @Provides
    @MediumDate
    fun provideMediumDateFormatter(application: TiviApplication): DateTimeFormatter {
        val dateF = AndroidDateFormat.getMediumDateFormat(application) as SimpleDateFormat

        @Suppress("DEPRECATION")
        return DateTimeFormatter.ofPattern(dateF.toPattern())
                .withLocale(application.resources.configuration.locale)
                .withZone(ZoneId.systemDefault())
    }

    @Singleton
    @Provides
    @MediumDateTime
    fun provideDateTimeFormatter(application: TiviApplication): DateTimeFormatter {
        val dateF = AndroidDateFormat.getMediumDateFormat(application) as SimpleDateFormat
        val timeF = AndroidDateFormat.getTimeFormat(application) as SimpleDateFormat

        @Suppress("DEPRECATION")
        return DateTimeFormatter.ofPattern("${dateF.toPattern()} ${timeF.toPattern()}")
                .withLocale(application.resources.configuration.locale)
                .withZone(ZoneId.systemDefault())
    }

    @Singleton
    @Provides
    @ShortDate
    fun provideShortDateFormatter(application: TiviApplication): DateTimeFormatter {
        val dateF = AndroidDateFormat.getDateFormat(application) as SimpleDateFormat

        @Suppress("DEPRECATION")
        return DateTimeFormatter.ofPattern(dateF.toPattern())
                .withLocale(application.resources.configuration.locale)
                .withZone(ZoneId.systemDefault())
    }

    @Provides
    @ProcessLifetime
    fun provideLongLifetimeScope(): CoroutineScope {
        return ProcessLifecycleOwner.get().lifecycle.coroutineScope
    }

    @Provides
    @Singleton
    fun provideAppBarConfiguration() = AppBarConfiguration.Builder(
            R.id.navigation_followed,
            R.id.navigation_watched,
            R.id.navigation_discover,
            R.id.navigation_search
    ).build()
}
