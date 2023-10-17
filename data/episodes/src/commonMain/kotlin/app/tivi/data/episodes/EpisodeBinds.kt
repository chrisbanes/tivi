// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes

import app.tivi.data.episodes.datasource.EpisodeDataSource
import app.tivi.data.episodes.datasource.EpisodeWatchesDataSource
import app.tivi.data.episodes.datasource.SeasonsEpisodesDataSource
import app.tivi.data.episodes.datasource.TmdbEpisodeDataSourceImpl
import app.tivi.data.episodes.datasource.TmdbSeasonsEpisodesDataSourceImpl
import app.tivi.data.episodes.datasource.TraktEpisodeDataSourceImpl
import app.tivi.data.episodes.datasource.TraktEpisodeWatchesDataSource
import app.tivi.data.episodes.datasource.TraktSeasonsEpisodesDataSourceImpl
import me.tatarka.inject.annotations.Provides

interface EpisodeBinds {

  @Provides
  fun provideTraktEpisodeDataSource(
    bind: TraktEpisodeDataSourceImpl,
  ): TraktEpisodeDataSource = bind

  @Provides
  fun provideTmdbEpisodeDataSource(
    bind: TmdbEpisodeDataSourceImpl,
  ): TmdbEpisodeDataSource = bind

  @Provides
  fun provideTraktSeasonsEpisodesDataSource(
    bind: TraktSeasonsEpisodesDataSourceImpl,
  ): TraktSeasonsEpisodesDataSource = bind

  @Provides
  fun provideTmdbSeasonsEpisodesDataSource(
    bind: TmdbSeasonsEpisodesDataSourceImpl,
  ): TmdbSeasonsEpisodesDataSource = bind

  @Provides
  fun provideEpisodeWatchesDataSource(
    bind: TraktEpisodeWatchesDataSource,
  ): EpisodeWatchesDataSource = bind
}

typealias TmdbEpisodeDataSource = EpisodeDataSource
typealias TraktEpisodeDataSource = EpisodeDataSource

typealias TmdbSeasonsEpisodesDataSource = SeasonsEpisodesDataSource
typealias TraktSeasonsEpisodesDataSource = SeasonsEpisodesDataSource
