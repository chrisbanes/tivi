/*
 * Copyright 2019 Google LLC
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

@file:OptIn(ExperimentalCoroutinesApi::class)

package app.tivi.data

import app.tivi.data.episodes.EpisodeBinds
import app.tivi.data.episodes.SeasonsEpisodesDataSource
import app.tivi.data.episodes.TmdbEpisodeDataSource
import app.tivi.data.episodes.TmdbEpisodeDataSourceImpl
import app.tivi.data.episodes.TraktEpisodeDataSource
import app.tivi.data.episodes.TraktEpisodeDataSourceImpl
import app.tivi.data.episodes.TraktSeasonsEpisodesDataSource
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
import app.tivi.utils.SuccessFakeShowDataSource
import app.tivi.utils.SuccessFakeShowImagesDataSource
import io.mockk.mockk
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
    ): FollowedShowsDataSource = mockk()

    @ApplicationScope
    @Provides
    override fun provideTraktEpisodeDataSource(
        bind: TraktEpisodeDataSourceImpl,
    ): TraktEpisodeDataSource = mockk()

    @ApplicationScope
    @Provides
    override fun provideTmdbEpisodeDataSource(
        bind: TmdbEpisodeDataSourceImpl,
    ): TmdbEpisodeDataSource = mockk()

    @ApplicationScope
    @Provides
    override fun provideTraktSeasonsEpisodesDataSource(
        bind: TraktSeasonsEpisodesDataSource,
    ): SeasonsEpisodesDataSource = mockk()

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
            computation = testDispatcher,
            main = testDispatcher,
        )
    }

    @Provides
    fun provideAuthStore(): AuthStore = AuthorizedAuthStore
}

interface TestDatabaseComponent : SqlDelightDatabaseComponent {

    @Provides
    fun provideDriverFactory(): DriverFactory = DriverFactory()
}
