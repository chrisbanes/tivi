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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import app.tivi.AppNavigator
import app.tivi.TiviAppActivityNavigator
import app.tivi.home.main.HomeNavigationBuilder
import app.tivi.inject.PerActivity
import app.tivi.inject.ViewModelBuilder
import app.tivi.inject.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
internal abstract class HomeBuilder {
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class,
        HomeModule::class,
        HomeNavigationBuilder::class
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

@Module(includes = [HomeModuleBinds::class])
class HomeModule {
    @Provides
    fun provideHomeNavigator(
        activity: HomeActivity,
        factory: ViewModelProvider.Factory
    ): HomeNavigator {
        return ViewModelProviders.of(activity, factory).get(HomeNavigatorViewModel::class.java)
    }

    @Provides
    fun provideAppNavigator(activity: HomeActivity): AppNavigator {
        return TiviAppActivityNavigator(activity)
    }
}

@Module
abstract class HomeModuleBinds {
    @Binds
    @PerActivity
    abstract fun bindContext(activity: HomeActivity): Context
}