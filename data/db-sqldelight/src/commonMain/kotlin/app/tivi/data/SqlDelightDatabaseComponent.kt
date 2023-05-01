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

package app.tivi.data

import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.LibraryShowsDao
import app.tivi.data.daos.PopularDao
import app.tivi.data.daos.RecommendedDao
import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.ShowFtsDao
import app.tivi.data.daos.ShowTmdbImagesDao
import app.tivi.data.daos.SqlDelightEpisodeWatchEntryDao
import app.tivi.data.daos.SqlDelightEpisodesDao
import app.tivi.data.daos.SqlDelightFollowedShowsDao
import app.tivi.data.daos.SqlDelightLastRequestDao
import app.tivi.data.daos.SqlDelightLibraryShowsDao
import app.tivi.data.daos.SqlDelightPopularShowsDao
import app.tivi.data.daos.SqlDelightRecommendedShowsDao
import app.tivi.data.daos.SqlDelightRelatedShowsDao
import app.tivi.data.daos.SqlDelightSeasonsDao
import app.tivi.data.daos.SqlDelightShowFtsDao
import app.tivi.data.daos.SqlDelightShowImagesDao
import app.tivi.data.daos.SqlDelightTiviShowDao
import app.tivi.data.daos.SqlDelightTrendingShowsDao
import app.tivi.data.daos.SqlDelightUserDao
import app.tivi.data.daos.SqlDelightWatchedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.TrendingDao
import app.tivi.data.daos.UserDao
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface SqlDelightDatabaseComponent {
    @ApplicationScope
    @Provides
    fun provideSqlDelightDatabase(
        factory: DatabaseFactory,
    ): Database = factory.build()

    @ApplicationScope
    @Provides
    fun bindTiviShowDao(dao: SqlDelightTiviShowDao): TiviShowDao = dao

    @ApplicationScope
    @Provides
    fun bindUserDao(dao: SqlDelightUserDao): UserDao = dao

    @ApplicationScope
    @Provides
    fun bindTrendingDao(dao: SqlDelightTrendingShowsDao): TrendingDao = dao

    @ApplicationScope
    @Provides
    fun bindPopularDao(dao: SqlDelightPopularShowsDao): PopularDao = dao

    @ApplicationScope
    @Provides
    fun bindWatchedShowDao(dao: SqlDelightWatchedShowsDao): WatchedShowDao = dao

    @ApplicationScope
    @Provides
    fun bindFollowedShowsDao(dao: SqlDelightFollowedShowsDao): FollowedShowsDao = dao

    @ApplicationScope
    @Provides
    fun bindSeasonsDao(dao: SqlDelightSeasonsDao): SeasonsDao = dao

    @ApplicationScope
    @Provides
    fun bindEpisodesDao(dao: SqlDelightEpisodesDao): EpisodesDao = dao

    @ApplicationScope
    @Provides
    fun bindRelatedShowsDao(dao: SqlDelightRelatedShowsDao): RelatedShowsDao = dao

    @ApplicationScope
    @Provides
    fun bindEpisodeWatchEntryDao(dao: SqlDelightEpisodeWatchEntryDao): EpisodeWatchEntryDao = dao

    @ApplicationScope
    @Provides
    fun bindLastRequestDao(dao: SqlDelightLastRequestDao): LastRequestDao = dao

    @ApplicationScope
    @Provides
    fun bindShowTmdbImagesDao(dao: SqlDelightShowImagesDao): ShowTmdbImagesDao = dao

    @ApplicationScope
    @Provides
    fun bindShowFtsDao(dao: SqlDelightShowFtsDao): ShowFtsDao = dao

    @ApplicationScope
    @Provides
    fun bindRecommendedDao(dao: SqlDelightRecommendedShowsDao): RecommendedDao = dao

    @ApplicationScope
    @Provides
    fun bindLibraryShowsDao(dao: SqlDelightLibraryShowsDao): LibraryShowsDao = dao

    @ApplicationScope
    @Provides
    fun provideDatabaseTransactionRunner(runner: SqlDelightTransactionRunner): DatabaseTransactionRunner = runner
}
