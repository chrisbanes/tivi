// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalCoroutinesApi::class)

package app.tivi.data

import app.tivi.data.episodes.EpisodeBinds
import app.tivi.data.episodes.TmdbEpisodeDataSource
import app.tivi.data.episodes.TmdbSeasonsEpisodesDataSource
import app.tivi.data.episodes.TraktEpisodeDataSource
import app.tivi.data.episodes.TraktSeasonsEpisodesDataSource
import app.tivi.data.episodes.datasource.EpisodeWatchesDataSource
import app.tivi.data.episodes.datasource.TmdbEpisodeDataSourceImpl
import app.tivi.data.episodes.datasource.TmdbSeasonsEpisodesDataSourceImpl
import app.tivi.data.episodes.datasource.TraktEpisodeDataSourceImpl
import app.tivi.data.episodes.datasource.TraktEpisodeWatchesDataSource
import app.tivi.data.episodes.datasource.TraktSeasonsEpisodesDataSourceImpl
import app.tivi.data.followedshows.FollowedShowsBinds
import app.tivi.data.followedshows.FollowedShowsDataSource
import app.tivi.data.followedshows.TraktFollowedShowsDataSource
import app.tivi.data.showimages.ShowImagesBinds
import app.tivi.data.showimages.ShowImagesDataSource
import app.tivi.data.showimages.TmdbShowImagesDataSource
import app.tivi.data.shows.ShowsBinds
import app.tivi.data.shows.TmdbShowDataSource
import app.tivi.data.shows.TmdbShowDataSourceImpl
import app.tivi.data.shows.TraktShowDataSource
import app.tivi.data.shows.TraktShowDataSourceImpl
import app.tivi.data.traktauth.store.AuthStore
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.utils.AuthorizedAuthStore
import app.tivi.utils.FakeEpisodeDataSource
import app.tivi.utils.FakeEpisodeWatchesDataSource
import app.tivi.utils.FakeFollowedShowsDataSource
import app.tivi.utils.FakeSeasonsEpisodesDataSource
import app.tivi.utils.SuccessFakeShowDataSource
import app.tivi.utils.SuccessFakeShowImagesDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import me.tatarka.inject.annotations.Provides

abstract class TestDataSourceComponent :
  FollowedShowsBinds,
  EpisodeBinds,
  ShowsBinds,
  ShowImagesBinds {

  @ApplicationScope
  @Provides
  override fun provideTraktFollowedShowsDataSource(
    bind: TraktFollowedShowsDataSource,
  ): FollowedShowsDataSource = FakeFollowedShowsDataSource()

  @ApplicationScope
  @Provides
  override fun provideTraktEpisodeDataSource(
    bind: TraktEpisodeDataSourceImpl,
  ): TraktEpisodeDataSource = FakeEpisodeDataSource()

  @ApplicationScope
  @Provides
  override fun provideTmdbEpisodeDataSource(
    bind: TmdbEpisodeDataSourceImpl,
  ): TmdbEpisodeDataSource = FakeEpisodeDataSource()

  @ApplicationScope
  @Provides
  override fun provideTraktSeasonsEpisodesDataSource(
    bind: TraktSeasonsEpisodesDataSourceImpl,
  ): TraktSeasonsEpisodesDataSource = FakeSeasonsEpisodesDataSource()

  @ApplicationScope
  @Provides
  override fun provideTmdbSeasonsEpisodesDataSource(
    bind: TmdbSeasonsEpisodesDataSourceImpl,
  ): TmdbSeasonsEpisodesDataSource = FakeSeasonsEpisodesDataSource()

  @ApplicationScope
  @Provides
  override fun provideEpisodeWatchesDataSource(
    bind: TraktEpisodeWatchesDataSource,
  ): EpisodeWatchesDataSource = FakeEpisodeWatchesDataSource()

  @ApplicationScope
  @Provides
  override fun bindTraktShowDataSource(
    bind: TraktShowDataSourceImpl,
  ): TraktShowDataSource = SuccessFakeShowDataSource

  @ApplicationScope
  @Provides
  override fun bindTmdbShowDataSource(
    bind: TmdbShowDataSourceImpl,
  ): TmdbShowDataSource = SuccessFakeShowDataSource

  @ApplicationScope
  @Provides
  override fun bindShowImagesDataSource(
    bind: TmdbShowImagesDataSource,
  ): ShowImagesDataSource = SuccessFakeShowImagesDataSource

  @ApplicationScope
  @Provides
  fun provideAppCoroutineDispatchers(): AppCoroutineDispatchers {
    val testDispatcher = UnconfinedTestDispatcher()
    return AppCoroutineDispatchers(
      io = testDispatcher,
      databaseRead = testDispatcher,
      databaseWrite = testDispatcher,
      computation = testDispatcher,
      main = testDispatcher,
    )
  }

  @Provides
  fun provideAuthStore(): AuthStore = AuthorizedAuthStore
}

interface TestDatabaseComponent : SqlDelightDatabaseComponent
