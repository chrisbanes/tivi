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

package me.banes.chris.tivi.home

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import me.banes.chris.tivi.home.discover.DiscoverBuilder
import me.banes.chris.tivi.home.library.LibraryBuilder
import me.banes.chris.tivi.home.popular.PopularBuilder
import me.banes.chris.tivi.home.trending.TrendingBuilder
import me.banes.chris.tivi.home.watched.MyShowsBuilder
import me.banes.chris.tivi.home.watched.WatchedShowsBuilder
import me.banes.chris.tivi.inject.ViewModelKey

@Module
internal abstract class HomeBuilder {
    @ContributesAndroidInjector(modules = [
        HomeModule::class,
        DiscoverBuilder::class,
        TrendingBuilder::class,
        PopularBuilder::class,
        LibraryBuilder::class,
        WatchedShowsBuilder::class,
        MyShowsBuilder::class
    ])
    internal abstract fun homeActivity(): HomeActivity

    @Binds
    @IntoMap
    @ViewModelKey(HomeActivityViewModel::class)
    abstract fun bindHomeActivityViewModel(viewModel: HomeActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeNavigatorViewModel::class)
    abstract fun bindHomeNavigatorViewModel(viewModel: HomeNavigatorViewModel): ViewModel
}