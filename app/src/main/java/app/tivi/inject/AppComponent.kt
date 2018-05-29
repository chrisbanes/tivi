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

import app.tivi.TiviApplication
import app.tivi.data.DatabaseModule
import app.tivi.home.HomeBuilder
import app.tivi.showdetails.ShowDetailsBuilder
import app.tivi.tasks.JobsCreator
import app.tivi.tmdb.TmdbModule
import app.tivi.trakt.TraktAuthModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    JobsCreator::class,
    DatabaseModule::class,
    ViewModelBuilder::class,
    HomeBuilder::class,
    ShowDetailsBuilder::class,
    TraktAuthModule::class,
    TmdbModule::class,
    NetworkModule::class
])
interface AppComponent : AndroidInjector<TiviApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<TiviApplication>()
}