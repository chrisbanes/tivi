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

package me.banes.chris.tivi.inject

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import me.banes.chris.tivi.TiviApplication
import me.banes.chris.tivi.data.DatabaseModule
import me.banes.chris.tivi.details.ShowDetailsBuilder
import me.banes.chris.tivi.home.HomeBuilder
import me.banes.chris.tivi.jobs.JobsCreator
import me.banes.chris.tivi.tmdb.TmdbModule
import me.banes.chris.tivi.trakt.TraktModule
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
    TraktModule::class,
    TmdbModule::class,
    NetworkModule::class
])
interface AppComponent : AndroidInjector<TiviApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<TiviApplication>()
}