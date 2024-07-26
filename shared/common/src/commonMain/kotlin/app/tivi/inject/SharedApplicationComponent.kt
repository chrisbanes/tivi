// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.appinitializers.AppInitializers
import app.tivi.common.imageloading.ImageLoadingComponent
import app.tivi.core.analytics.AnalyticsComponent
import app.tivi.core.notifications.NotificationsComponent
import app.tivi.core.perf.PerformanceComponent
import app.tivi.core.permissions.PermissionsComponent
import app.tivi.data.SqlDelightDatabaseComponent
import app.tivi.data.episodes.EpisodeBinds
import app.tivi.data.followedshows.FollowedShowsBinds
import app.tivi.data.licenses.LicenseDataComponent
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
import app.tivi.entitlements.EntitlementsComponent
import app.tivi.navigation.DeepLinker
import app.tivi.settings.PreferencesComponent
import app.tivi.tasks.TasksComponent
import app.tivi.tmdb.TmdbComponent
import app.tivi.trakt.TraktComponent
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.LoggerComponent
import app.tivi.util.PowerControllerComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Provides

expect interface SharedPlatformApplicationComponent

interface SharedApplicationComponent :
  SharedPlatformApplicationComponent,
  TasksComponent,
  ImageLoadingComponent,
  TmdbComponent,
  TraktComponent,
  AnalyticsComponent,
  EntitlementsComponent,
  LoggerComponent,
  NotificationsComponent,
  PerformanceComponent,
  PermissionsComponent,
  PowerControllerComponent,
  PreferencesComponent,
  LicenseDataComponent,
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
  SqlDelightDatabaseComponent {

  val initializers: AppInitializers
  val dispatchers: AppCoroutineDispatchers
  val deepLinker: DeepLinker

  @OptIn(ExperimentalCoroutinesApi::class)
  @ApplicationScope
  @Provides
  fun provideCoroutineDispatchers(): AppCoroutineDispatchers = AppCoroutineDispatchers(
    io = Dispatchers.IO,
    databaseWrite = Dispatchers.IO.limitedParallelism(1),
    databaseRead = Dispatchers.IO.limitedParallelism(4),
    computation = Dispatchers.Default,
    main = Dispatchers.Main,
  )

  @ApplicationScope
  @Provides
  fun provideApplicationCoroutineScope(
    dispatchers: AppCoroutineDispatchers,
  ): ApplicationCoroutineScope = CoroutineScope(dispatchers.main + SupervisorJob())
}
