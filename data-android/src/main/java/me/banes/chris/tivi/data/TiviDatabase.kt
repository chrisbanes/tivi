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

package me.banes.chris.tivi.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import me.banes.chris.tivi.data.daos.EpisodesDao
import me.banes.chris.tivi.data.daos.MyShowsDao
import me.banes.chris.tivi.data.daos.PopularDao
import me.banes.chris.tivi.data.daos.SeasonsDao
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.daos.TrendingDao
import me.banes.chris.tivi.data.daos.UserDao
import me.banes.chris.tivi.data.daos.WatchedShowDao
import me.banes.chris.tivi.data.entities.Episode
import me.banes.chris.tivi.data.entities.MyShowsEntry
import me.banes.chris.tivi.data.entities.PopularEntry
import me.banes.chris.tivi.data.entities.Season
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.data.entities.TraktUser
import me.banes.chris.tivi.data.entities.TrendingEntry
import me.banes.chris.tivi.data.entities.WatchedShowEntry

@Database(
        entities = [
            TiviShow::class,
            TrendingEntry::class,
            PopularEntry::class,
            TraktUser::class,
            WatchedShowEntry::class,
            MyShowsEntry::class,
            Season::class,
            Episode::class
        ],
        version = 13
)
@TypeConverters(TiviTypeConverters::class)
abstract class TiviDatabase : RoomDatabase() {
    abstract fun showDao(): TiviShowDao
    abstract fun trendingDao(): TrendingDao
    abstract fun popularDao(): PopularDao
    abstract fun userDao(): UserDao
    abstract fun watchedShowsDao(): WatchedShowDao
    abstract fun myShowsDao(): MyShowsDao
    abstract fun seasonsDao(): SeasonsDao
    abstract fun episodesDao(): EpisodesDao
}