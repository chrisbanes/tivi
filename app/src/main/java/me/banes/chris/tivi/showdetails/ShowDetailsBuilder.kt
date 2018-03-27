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

package me.banes.chris.tivi.showdetails

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import me.banes.chris.tivi.inject.ViewModelKey
import me.banes.chris.tivi.showdetails.details.ShowDetailsFragmentBuilder

@Module
internal abstract class ShowDetailsBuilder {
    @ContributesAndroidInjector(modules = [ShowDetailsModule::class, ShowDetailsFragmentBuilder::class])
    internal abstract fun showDetailsActivity(): ShowDetailsActivity

    @Binds
    @IntoMap
    @ViewModelKey(ShowDetailsNavigatorViewModel::class)
    abstract fun bindDetailsNavigatorViewModel(viewModel: ShowDetailsNavigatorViewModel): ViewModel
}