/*
 * Copyright 2017 Google LLC
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

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import app.tivi.data.daos.RoomEpisodeWatchEntryDao
import app.tivi.data.daos.RoomEpisodesDao
import app.tivi.data.daos.RoomFollowedShowsDao
import app.tivi.data.daos.RoomLastRequestDao
import app.tivi.data.daos.RoomLibraryShowsDao
import app.tivi.data.daos.RoomPopularDao
import app.tivi.data.daos.RoomRecommendedDao
import app.tivi.data.daos.RoomRelatedShowsDao
import app.tivi.data.daos.RoomSeasonsDao
import app.tivi.data.daos.RoomShowFtsDao
import app.tivi.data.daos.RoomShowTmdbImagesDao
import app.tivi.data.daos.RoomTiviShowDao
import app.tivi.data.daos.RoomTrendingDao
import app.tivi.data.daos.RoomUserDao
import app.tivi.data.daos.RoomWatchedShowDao
import app.tivi.data.db.DateTimeTypeConverters
import app.tivi.data.db.TiviDatabase
import app.tivi.data.db.TiviTypeConverters
import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.LastRequest
import app.tivi.data.models.PopularShowEntry
import app.tivi.data.models.RecommendedShowEntry
import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.models.Season
import app.tivi.data.models.ShowTmdbImage
import app.tivi.data.models.TiviShow
import app.tivi.data.models.TiviShowFts
import app.tivi.data.models.TraktUser
import app.tivi.data.models.TrendingShowEntry
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.views.ShowsLastWatched
import app.tivi.data.views.ShowsNextToWatch
import app.tivi.data.views.ShowsWatchStats

@Database(
    entities = [
        TiviShow::class,
        TiviShowFts::class,
        TrendingShowEntry::class,
        PopularShowEntry::class,
        TraktUser::class,
        WatchedShowEntry::class,
        FollowedShowEntry::class,
        Season::class,
        Episode::class,
        RelatedShowEntry::class,
        EpisodeWatchEntry::class,
        LastRequest::class,
        ShowTmdbImage::class,
        RecommendedShowEntry::class,
    ],
    views = [
        ShowsWatchStats::class,
        ShowsLastWatched::class,
        ShowsNextToWatch::class,
    ],
    version = 32,
    autoMigrations = [
        AutoMigration(from = 24, to = 25),
        AutoMigration(from = 25, to = 26),
        AutoMigration(from = 26, to = 27),
        AutoMigration(from = 27, to = 28), // can remove this later
        AutoMigration(from = 28, to = 29), // can remove this later
        AutoMigration(from = 29, to = 30), // can remove this later
        AutoMigration(from = 27, to = 30),
        AutoMigration(from = 30, to = 31, spec = TiviRoomDatabase.AutoMigrationSpec31::class),
        AutoMigration(from = 31, to = 32),
    ],
)
@TypeConverters(TiviTypeConverters::class, DateTimeTypeConverters::class)
abstract class TiviRoomDatabase : RoomDatabase(), TiviDatabase {

    @DeleteColumn.Entries(
        DeleteColumn(tableName = "shows", columnName = "last_trakt_data_update"),
    )
    class AutoMigrationSpec31 : AutoMigrationSpec

    abstract override fun showDao(): RoomTiviShowDao
    abstract override fun showFtsDao(): RoomShowFtsDao
    abstract override fun showImagesDao(): RoomShowTmdbImagesDao
    abstract override fun trendingDao(): RoomTrendingDao
    abstract override fun popularDao(): RoomPopularDao
    abstract override fun userDao(): RoomUserDao
    abstract override fun watchedShowsDao(): RoomWatchedShowDao
    abstract override fun followedShowsDao(): RoomFollowedShowsDao
    abstract override fun seasonsDao(): RoomSeasonsDao
    abstract override fun episodesDao(): RoomEpisodesDao
    abstract override fun relatedShowsDao(): RoomRelatedShowsDao
    abstract override fun episodeWatchesDao(): RoomEpisodeWatchEntryDao
    abstract override fun lastRequestDao(): RoomLastRequestDao
    abstract override fun recommendedShowsDao(): RoomRecommendedDao
    abstract override fun libraryShowsDao(): RoomLibraryShowsDao
}
