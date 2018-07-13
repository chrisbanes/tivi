/*
 * Copyright 2017 Google, Inc.
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

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.PopularDao
import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.TrendingDao
import app.tivi.data.daos.UserDao
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.LastRequest
import app.tivi.data.entities.PopularShowEntry
import app.tivi.data.entities.RelatedShowEntry
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TraktUser
import app.tivi.data.entities.TrendingShowEntry
import app.tivi.data.entities.WatchedShowEntry

@Database(
        entities = [
            TiviShow::class,
            TrendingShowEntry::class,
            PopularShowEntry::class,
            TraktUser::class,
            WatchedShowEntry::class,
            FollowedShowEntry::class,
            Season::class,
            Episode::class,
            RelatedShowEntry::class,
            EpisodeWatchEntry::class,
            LastRequest::class
        ],
        version = 9
)
@TypeConverters(TiviTypeConverters::class)
abstract class TiviDatabase : RoomDatabase() {
    abstract fun showDao(): TiviShowDao
    abstract fun trendingDao(): TrendingDao
    abstract fun popularDao(): PopularDao
    abstract fun userDao(): UserDao
    abstract fun watchedShowsDao(): WatchedShowDao
    abstract fun followedShowsDao(): FollowedShowsDao
    abstract fun seasonsDao(): SeasonsDao
    abstract fun episodesDao(): EpisodesDao
    abstract fun relatedShowsDao(): RelatedShowsDao
    abstract fun episodeWatchesDao(): EpisodeWatchEntryDao
    abstract fun lastRequestDao(): LastRequestDao
}