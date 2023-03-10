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
import app.tivi.BuildConfig
import app.tivi.app.ApplicationInfo
import app.tivi.appinitializers.AppInitializer
import app.tivi.appinitializers.EmojiInitializer
import app.tivi.appinitializers.PreferencesInitializer
import app.tivi.appinitializers.TimberInitializer
import app.tivi.appinitializers.TmdbInitializer
import app.tivi.tmdb.TmdbOAuthInfo
import app.tivi.trakt.TraktConstants
import app.tivi.trakt.TraktOAuthInfo
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
        redirectUri = "${appInfo.packageName}://${TraktConstants.URI_AUTH_CALLBACK_PATH}",
    )

    @Provides
    @IntoSet
    fun provideEmojiInitializer(bind: EmojiInitializer): AppInitializer = bind

    @Provides
    @IntoSet
    fun provideTimberInitializer(bind: TimberInitializer): AppInitializer = bind

    @Provides
    @IntoSet
    fun providePreferencesInitializer(bind: PreferencesInitializer): AppInitializer = bind

    @Provides
    @IntoSet
    fun provideTmdbInitializer(bind: TmdbInitializer): AppInitializer = bind
}
