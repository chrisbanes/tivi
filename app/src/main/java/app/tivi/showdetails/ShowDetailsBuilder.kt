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

package app.tivi.showdetails

import app.tivi.episodedetails.EpisodeDetailsFragmentBuilder
import app.tivi.showdetails.details.ShowDetailsFragmentBuilder
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class ShowDetailsBuilder {
    @ContributesAndroidInjector(modules = [
        ShowDetailsModule::class,
        ShowDetailsFragmentBuilder::class,
        EpisodeDetailsFragmentBuilder::class
    ])
    internal abstract fun bindDetailsActivity(): ShowDetailsActivity
}
