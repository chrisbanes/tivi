/*
 * Copyright 2018 Google, Inc.
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

import app.tivi.data.repositories.episodes.EpisodeDataSource
import app.tivi.data.repositories.episodes.SeasonsEpisodesDataSource
import app.tivi.data.repositories.episodes.TmdbSeasonsEpisodesDataSource
import app.tivi.data.repositories.episodes.TraktSeasonsEpisodesDataSource
import app.tivi.data.repositories.followedshows.FollowedShowsDataSource
import app.tivi.data.repositories.followedshows.TraktFollowedShowsDataSource
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.data.repositories.shows.ShowRepositoryImpl
import app.tivi.inject.Tmdb
import app.tivi.inject.Trakt
import dagger.Binds
import dagger.Module

@Module
abstract class DataModule {
    @Binds
    abstract fun bind(source: ShowRepositoryImpl): ShowRepository

    @Binds
    @Trakt
    abstract fun bind(source: TraktFollowedShowsDataSource): FollowedShowsDataSource

    @Binds
    @Trakt
    abstract fun bind(source: TraktSeasonsEpisodesDataSource): SeasonsEpisodesDataSource

    @Binds
    @Trakt
    abstract fun bindTrakt(source: TraktSeasonsEpisodesDataSource): EpisodeDataSource

    @Binds
    @Tmdb
    abstract fun bindTmdb(source: TmdbSeasonsEpisodesDataSource): EpisodeDataSource
}