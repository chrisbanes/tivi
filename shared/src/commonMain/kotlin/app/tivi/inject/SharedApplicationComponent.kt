// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.appinitializers.AppInitializer
import app.tivi.appinitializers.TmdbInitializer
import app.tivi.common.imageloading.ImageLoadingComponent
import app.tivi.core.analytics.AnalyticsComponent
import app.tivi.core.perf.PerformanceComponent
import app.tivi.data.SqlDelightDatabaseComponent
import app.tivi.data.episodes.EpisodeBinds
import app.tivi.data.followedshows.FollowedShowsBinds
import app.tivi.data.popularshows.PopularShowsBinds
import app.tivi.data.recommendedshows.RecommendedShowsBinds
import app.tivi.data.relatedshows.RelatedShowsBinds
import app.tivi.data.search.SearchBinds
import app.tivi.data.showimages.ShowImagesBinds
import app.tivi.data.shows.ShowsBinds
import app.tivi.data.traktauth.TraktAuthComponent
import app.tivi.data.traktusers.TraktUsersBinds
import app.tivi.data.trendingshows.TrendingShowsBinds
import app.tivi.data.watchedshows.WatchedShowsBinds
import app.tivi.settings.PreferencesComponent
import app.tivi.tasks.TasksComponent
import app.tivi.tmdb.TmdbComponent
import app.tivi.trakt.TraktComponent
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.LoggerComponent
import app.tivi.util.PowerControllerComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface SharedApplicationComponent :
    ApiComponent,
    TasksComponent,
    CoreComponent,
    DataComponent,
    ImageLoadingComponent

interface ApiComponent : TmdbComponent, TraktComponent

interface CoreComponent :
    AnalyticsComponent,
    LoggerComponent,
    PerformanceComponent,
    PowerControllerComponent,
    PreferencesComponent {

    /**
     * Need to wait to upgrade to Coroutines 1.7.x so we can reference IO from common
     */
    @ApplicationScope
    @Provides
    fun provideCoroutineDispatchers(): AppCoroutineDispatchers = AppCoroutineDispatchers(
        io = Dispatchers.IO,
        computation = Dispatchers.Default,
        main = Dispatchers.Main,
    )

    @Provides
    @IntoSet
    fun provideTmdbInitializer(bind: TmdbInitializer): AppInitializer = bind
}

interface DataComponent :
    EpisodeBinds,
    FollowedShowsBinds,
    PopularShowsBinds,
    RecommendedShowsBinds,
    RelatedShowsBinds,
    SearchBinds,
    ShowImagesBinds,
    ShowsBinds,
    TraktAuthComponent,
    TraktUsersBinds,
    TrendingShowsBinds,
    WatchedShowsBinds,
    SqlDelightDatabaseComponent
