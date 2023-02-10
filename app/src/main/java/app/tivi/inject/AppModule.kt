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
import app.tivi.appinitializers.ThreeTenBpInitializer
import app.tivi.appinitializers.TimberInitializer
import app.tivi.appinitializers.TmdbInitializer
import app.tivi.util.AndroidPowerController
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.PowerController
import java.io.File
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

@Component
abstract class AppModule {
    @Provides
    fun provideApplicationId(application: Application): ApplicationInfo {
        return ApplicationInfo(application.packageName)
    }

    @Provides
    fun provideCoroutineDispatchers(): AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main,
    )

    @Provides
    @Named("cache")
    fun provideCacheDir(context: Application): File = context.cacheDir

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
    fun providePowerController(bind: AndroidPowerController): PowerController = bind

    @Provides
    @IntoSet
    fun provideEmojiInitializer(bind: EmojiInitializer): AppInitializer = bind

    @Provides
    @IntoSet
    fun provideThreeTenAbpInitializer(bind: ThreeTenBpInitializer): AppInitializer = bind

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
