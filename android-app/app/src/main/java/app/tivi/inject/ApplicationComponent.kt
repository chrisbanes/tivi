// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import android.app.Application
import android.content.Context
import app.tivi.TiviApplication
import app.tivi.appinitializers.AppInitializers
import app.tivi.common.imageloading.ImageLoadingComponent
import app.tivi.core.analytics.AnalyticsComponent
import app.tivi.core.perf.PerformanceComponent
import app.tivi.data.AndroidSqlDelightDatabaseComponent
import app.tivi.data.episodes.EpisodeBinds
import app.tivi.data.followedshows.FollowedShowsBinds
import app.tivi.data.popularshows.PopularShowsBinds
import app.tivi.data.recommendedshows.RecommendedShowsBinds
import app.tivi.data.search.SearchBinds
import app.tivi.data.showimages.ShowImagesBinds
import app.tivi.data.shows.ShowsBinds
import app.tivi.data.traktauth.RelatedShowsBinds
import app.tivi.data.traktauth.TraktAuthComponent
import app.tivi.data.traktusers.TraktUsersBinds
import app.tivi.data.trendingshows.TrendingShowsBinds
import app.tivi.data.watchedshows.WatchedShowsBinds
import app.tivi.home.ContentViewSetterComponent
import app.tivi.settings.PreferencesComponent
import app.tivi.tasks.TasksComponent
import app.tivi.tasks.TiviWorkerFactory
import app.tivi.tmdb.TmdbComponent
import app.tivi.trakt.TraktComponent
import app.tivi.util.LoggerComponent
import app.tivi.util.PowerControllerComponent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ApplicationScope
abstract class ApplicationComponent(
    @get:Provides val application: Application,
) : AndroidSqlDelightDatabaseComponent,
    TraktAuthComponent,
    TmdbComponent,
    TraktComponent,
    AppComponent,
    NetworkComponent,
    TasksComponent,
    PowerControllerComponent,
    ImageLoadingComponent,
    AnalyticsComponent,
    PerformanceComponent,
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
