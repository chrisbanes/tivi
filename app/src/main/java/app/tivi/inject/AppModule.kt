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

import android.app.Application
import android.content.Context
import app.tivi.BuildConfig
import app.tivi.extensions.withLocale
import app.tivi.tmdb.TmdbModule
import app.tivi.trakt.TraktModule
import app.tivi.util.AppCoroutineDispatchers
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(
    includes = [
        TraktModule::class,
        TmdbModule::class
    ]
)
object AppModule {
    @ApplicationId
    @Provides
    fun provideApplicationId(application: Application): String = application.packageName

    @Singleton
    @Provides
    fun provideCoroutineDispatchers() = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main
    )

    @Provides
    @Singleton
    @Named("cache")
    fun provideCacheDir(
        @ApplicationContext context: Context
    ): File = context.cacheDir

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
    @MediumDate
    fun provideMediumDateFormatter(
        @ApplicationContext context: Context
    ): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(context)
    }

    @Singleton
    @Provides
    @MediumDateTime
    fun provideDateTimeFormatter(
        @ApplicationContext context: Context
    ): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(context)
    }

    @Singleton
    @Provides
    @ShortDate
    fun provideShortDateFormatter(
        @ApplicationContext context: Context
    ): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(context)
    }

    @Singleton
    @Provides
    @ShortTime
    fun provideShortTimeFormatter(
        @ApplicationContext context: Context
    ): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
}
