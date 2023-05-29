// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes

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
        bind: TraktSeasonsEpisodesDataSource,
    ): SeasonsEpisodesDataSource = bind
}

typealias TmdbEpisodeDataSource = EpisodeDataSource
typealias TraktEpisodeDataSource = EpisodeDataSource
