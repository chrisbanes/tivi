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

package app.tivi.data.views

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import app.tivi.data.models.Season

@DatabaseView(
    viewName = "shows_view_watch_stats",
    value = """
        SELECT shows.id AS show_id, COUNT(*) AS episode_count, COUNT(ew.watched_at) AS watched_episode_count
        FROM shows
        INNER JOIN seasons AS s ON shows.id = s.show_id
        INNER JOIN episodes AS eps ON eps.season_id = s.id
        LEFT JOIN episode_watch_entries as ew ON ew.episode_id = eps.id
        WHERE eps.first_aired IS NOT NULL
            AND datetime(eps.first_aired) < datetime('now')
            AND s.number != ${Season.NUMBER_SPECIALS}
            AND s.ignored = 0
        GROUP BY shows.id
    """,
)
data class ShowsWatchStats(
    @ColumnInfo(name = "show_id") val showId: Long,
    @ColumnInfo(name = "episode_count") val episodeCount: Int,
    @ColumnInfo(name = "watched_episode_count") val watchedEpisodeCount: Int,
)
