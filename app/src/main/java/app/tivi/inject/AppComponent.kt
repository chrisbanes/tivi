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

import app.tivi.TiviApplication
import app.tivi.common.epoxy.EpoxyModule
import app.tivi.common.imageloading.ImageLoadingModule
import app.tivi.data.DataModule
import app.tivi.data.DatabaseModule
import app.tivi.home.HomeBuilder
import app.tivi.settings.SettingsPreferenceFragmentBuilder
import app.tivi.showdetails.ShowDetailsBuilder
import app.tivi.tasks.inject.TasksModule
import app.tivi.tmdb.TmdbModule
import app.tivi.trakt.TraktAuthModule
import app.tivi.trakt.TraktModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        TasksModule::class,
        AppAssistedModule::class,
        DatabaseModule::class,
        DataModule::class,
        HomeBuilder::class,
        ShowDetailsBuilder::class,
        TraktModule::class,
        TraktAuthModule::class,
        TmdbModule::class,
        NetworkModule::class,
        ImageLoadingModule::class,
        EpoxyModule::class,
        SettingsPreferenceFragmentBuilder::class
    ]
)
interface AppComponent : AndroidInjector<TiviApplication> {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: TiviApplication): AppComponent
    }
}
