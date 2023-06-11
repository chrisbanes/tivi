// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import android.app.Application
import app.tivi.BuildConfig
import app.tivi.app.ApplicationInfo
import app.tivi.appinitializers.AppInitializer
import app.tivi.appinitializers.EmojiInitializer
import app.tivi.appinitializers.LoggerInitializer
import app.tivi.appinitializers.TmdbInitializer
import app.tivi.data.traktauth.TraktOAuthInfo
import app.tivi.tmdb.TmdbOAuthInfo
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface AppComponent {
    @ApplicationScope
    @Provides
    fun provideApplicationId(application: Application): ApplicationInfo {
        return ApplicationInfo(application.packageName)
    }

    @ApplicationScope
    @Provides
    fun provideCoroutineDispatchers(): AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main,
    )

    @ApplicationScope
    @Provides
    fun provideTmdbApiKey(): TmdbOAuthInfo = TmdbOAuthInfo(BuildConfig.TMDB_API_KEY)

    @ApplicationScope
    @Provides
    fun provideTraktOAuthInfo(
        appInfo: ApplicationInfo,
    ): TraktOAuthInfo = TraktOAuthInfo(
        clientId = BuildConfig.TRAKT_CLIENT_ID,
        clientSecret = BuildConfig.TRAKT_CLIENT_SECRET,
        redirectUri = "${appInfo.packageName}://auth/oauth2callback",
    )

    @Provides
    @IntoSet
    fun provideEmojiInitializer(bind: EmojiInitializer): AppInitializer = bind

    @Provides
    @IntoSet
    fun provideLoggerInitializer(bind: LoggerInitializer): AppInitializer = bind

    @Provides
    @IntoSet
    fun provideTmdbInitializer(bind: TmdbInitializer): AppInitializer = bind
}
