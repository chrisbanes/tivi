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

package app.tivi.utils

import androidx.room.Database
import androidx.room.TypeConverters
import app.tivi.data.TiviRoomDatabase
import app.tivi.data.daos.RoomShowFtsDao
import app.tivi.data.db.DateTimeTypeConverters
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
import app.tivi.data.models.TraktUser
import app.tivi.data.models.TrendingShowEntry
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.views.FollowedShowsLastWatched
import app.tivi.data.views.FollowedShowsNextToWatch
import app.tivi.data.views.FollowedShowsWatchStats

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
        LastRequest::class,
        ShowTmdbImage::class,
        RecommendedShowEntry::class,
        FakeTiviShowFts::class,
    ],
    views = [
        FollowedShowsWatchStats::class,
        FollowedShowsLastWatched::class,
        FollowedShowsNextToWatch::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(TiviTypeConverters::class, DateTimeTypeConverters::class)
abstract class TiviTestDatabase : TiviRoomDatabase() {
    override fun showFtsDao(): RoomShowFtsDao = object : RoomShowFtsDao() {
        override suspend fun search(filter: String): List<TiviShow> = emptyList()
    }
}
