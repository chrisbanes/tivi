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

package app.tivi.data

import app.tivi.data.repositories.episodes.EpisodesModule
import app.tivi.data.repositories.recommendedshows.RecommendedShowsModule
import app.tivi.data.repositories.showimages.ShowsImagesModule
import app.tivi.data.repositories.shows.ShowsModule
import app.tivi.data.repositories.trendingshows.TrendingShowsModule
import app.tivi.data.repositories.watchedshows.WatchedShowsModule
import app.tivi.inject.ForStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope

@Module(includes = [
    EpisodesModule::class,
    ShowsModule::class,
    ShowsImagesModule::class,
    TrendingShowsModule::class,
    WatchedShowsModule::class,
    RecommendedShowsModule::class
])
class DataModule {
    @ForStore
    @Singleton
    @Provides
    fun providesStoreDispatcher(): CoroutineScope = CoroutineScope(EmptyCoroutineContext)
}
