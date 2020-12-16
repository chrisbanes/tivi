/*
 * Copyright 2018 Google LLC
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

import app.tivi.appinitializers.AppInitializer
import app.tivi.appinitializers.ClearGlideCacheInitializer
import app.tivi.appinitializers.EmojiInitializer
import app.tivi.appinitializers.PreferencesInitializer
import app.tivi.appinitializers.ThreeTenBpInitializer
import app.tivi.appinitializers.TimberInitializer
import app.tivi.appinitializers.TmdbInitializer
import app.tivi.util.AndroidPowerController
import app.tivi.util.Logger
import app.tivi.util.PowerController
import app.tivi.util.TiviLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AppModuleBinds {
    @Binds
    internal abstract fun providePowerController(bind: AndroidPowerController): PowerController

    @Singleton
    @Binds
    abstract fun provideLogger(bind: TiviLogger): Logger

    @Binds
    @IntoSet
    abstract fun provideEmojiInitializer(bind: EmojiInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun provideThreeTenAbpInitializer(bind: ThreeTenBpInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun provideTimberInitializer(bind: TimberInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun providePreferencesInitializer(bind: PreferencesInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun provideTmdbInitializer(bind: TmdbInitializer): AppInitializer

    @Binds
    @IntoSet
    abstract fun provideClearGlideInitializer(bind: ClearGlideCacheInitializer): AppInitializer
}
