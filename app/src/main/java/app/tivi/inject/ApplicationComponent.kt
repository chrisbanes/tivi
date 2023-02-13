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
import app.tivi.common.imageloading.ImageLoadingModule
import app.tivi.data.RoomDatabaseModule
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
import app.tivi.home.ContentViewSetterModule
import app.tivi.settings.SettingsModule
import app.tivi.tasks.TasksModule
import app.tivi.tasks.TiviWorkerFactory
import app.tivi.tmdb.TmdbModule
import app.tivi.trakt.TraktAuthModule
import app.tivi.trakt.TraktModule
import app.tivi.util.AnalyticsModule
import app.tivi.util.LoggerModule
import app.tivi.util.PowerControllerModule
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ApplicationScope
abstract class ApplicationComponent(
    @get:Provides val application: Application,
) : RoomDatabaseModule,
    TraktAuthModule,
    TmdbModule,
    TraktModule,
    AppModule,
    NetworkModule,
    TasksModule,
    PowerControllerModule,
    ImageLoadingModule,
    AnalyticsModule,
    SettingsModule,
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
    LoggerModule,
    ContentViewSetterModule,
    VariantAwareModule {

    abstract val initializers: AppInitializers
    abstract val workerFactory: TiviWorkerFactory

    companion object {
        fun from(context: Context): ApplicationComponent {
            return (context.applicationContext as TiviApplication).component
        }
    }
}
