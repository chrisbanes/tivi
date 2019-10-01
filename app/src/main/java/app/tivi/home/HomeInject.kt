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

package app.tivi.home

import android.content.Context
import app.tivi.AppNavigator
import app.tivi.home.discover.DiscoverBuilder
import app.tivi.home.followed.FollowedBuilder
import app.tivi.home.popular.PopularBuilder
import app.tivi.home.recommended.RecommendedBuilder
import app.tivi.home.search.SearchBuilder
import app.tivi.home.trending.TrendingBuilder
import app.tivi.home.watched.WatchedBuilder
import app.tivi.inject.PerActivity
import app.tivi.inject.ViewModelBuilder
import app.tivi.settings.SettingsFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class HomeBuilder {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class,
        HomeModule::class,
        SearchBuilder::class,
        DiscoverBuilder::class,
        TrendingBuilder::class,
        PopularBuilder::class,
        WatchedBuilder::class,
        FollowedBuilder::class,
        RecommendedBuilder::class
    ])
    internal abstract fun homeActivity(): HomeActivity
}

@Module(includes = [HomeModuleBinds::class])
class HomeModule {
    @Provides
    fun provideAppNavigator(activity: HomeActivity): AppNavigator = HomeAppNavigator(activity)
}

@Module
abstract class HomeModuleBinds {
    @Binds
    @PerActivity
    abstract fun bindContext(activity: HomeActivity): Context

    @ContributesAndroidInjector
    abstract fun settingsFragment(): SettingsFragment
}