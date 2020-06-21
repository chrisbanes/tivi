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

package app.tivi.data

import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.PopularDao
import app.tivi.data.daos.RecommendedDao
import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.ShowFtsDao
import app.tivi.data.daos.ShowTmdbImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.TrendingDao
import app.tivi.data.daos.UserDao
import app.tivi.data.daos.WatchedShowDao

interface TiviDatabase {
    fun showDao(): TiviShowDao
    fun showFtsDao(): ShowFtsDao
    fun showImagesDao(): ShowTmdbImagesDao
    fun trendingDao(): TrendingDao
    fun popularDao(): PopularDao
    fun userDao(): UserDao
    fun watchedShowsDao(): WatchedShowDao
    fun followedShowsDao(): FollowedShowsDao
    fun seasonsDao(): SeasonsDao
    fun episodesDao(): EpisodesDao
    fun relatedShowsDao(): RelatedShowsDao
    fun episodeWatchesDao(): EpisodeWatchEntryDao
    fun lastRequestDao(): LastRequestDao
    fun recommendedShowsDao(): RecommendedDao
}
