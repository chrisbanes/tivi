/*
 * Copyright 2023 Google LLC
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

import android.app.Application
import android.content.Context
import app.tivi.TiviApplication
import app.tivi.appinitializers.AppInitializers
import app.tivi.common.imageloading.ImageLoadingComponent
import app.tivi.data.RoomDatabaseComponent
import app.tivi.data.episodes.EpisodeBinds
import app.tivi.data.followedshows.FollowedShowsBinds
import app.tivi.data.popularshows.PopularShowsBinds
import app.tivi.data.recommendedshows.RecommendedShowsBinds
import app.tivi.data.relatedshows.RelatedShowsBinds
import app.tivi.data.search.SearchBinds
import app.tivi.data.showimages.ShowImagesBinds
import app.tivi.data.shows.ShowsBinds
import app.tivi.data.traktusers.TraktUsersBinds
import app.tivi.data.trendingshows.TrendingShowsBinds
import app.tivi.data.watchedshows.WatchedShowsBinds
import app.tivi.home.ContentViewSetterComponent
import app.tivi.settings.PreferencesComponent
import app.tivi.tasks.TasksComponent
import app.tivi.tasks.TiviWorkerFactory
import app.tivi.tmdb.TmdbComponent
import app.tivi.trakt.TraktAuthComponent
import app.tivi.trakt.TraktComponent
import app.tivi.util.AnalyticsComponent
import app.tivi.util.LoggerComponent
import app.tivi.util.PowerControllerComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ApplicationScope
abstract class ApplicationComponent(
    @get:Provides val application: Application,
) : RoomDatabaseComponent,
    TraktAuthComponent,
    TmdbComponent,
    TraktComponent,
    AppComponent,
    NetworkComponent,
    TasksComponent,
    PowerControllerComponent,
    ImageLoadingComponent,
    AnalyticsComponent,
    PreferencesComponent,
    EpisodeBinds,
    FollowedShowsBinds,
    PopularShowsBinds,
    RecommendedShowsBinds,
    RelatedShowsBinds,
    SearchBinds,
    ShowImagesBinds,
    ShowsBinds,
    TraktUsersBinds,
    TrendingShowsBinds,
    WatchedShowsBinds,
    LoggerComponent,
    ContentViewSetterComponent,
    VariantAwareComponent {

    abstract val initializers: AppInitializers
    abstract val workerFactory: TiviWorkerFactory

    companion object {
        fun from(context: Context): ApplicationComponent {
            return (context.applicationContext as TiviApplication).component
        }
    }
}
