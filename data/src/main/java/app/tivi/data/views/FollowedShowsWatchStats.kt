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

import androidx.room.DatabaseView
import app.tivi.data.entities.Season

@DatabaseView(
    """
    SELECT fs.id, COUNT(*) as episodeCount, COUNT(ew.watched_at) as watchedEpisodeCount
    FROM myshows_entries as fs
    INNER JOIN seasons AS s ON fs.show_id = s.show_id
    INNER JOIN episodes AS eps ON eps.season_id = s.id
    LEFT JOIN episode_watch_entries as ew ON ew.episode_id = eps.id
    WHERE eps.first_aired IS NOT NULL
        AND datetime(eps.first_aired) < datetime('now')
        AND s.number != ${Season.NUMBER_SPECIALS}
        AND s.ignored = 0
    GROUP BY fs.id
"""
)
data class FollowedShowsWatchStats(
    val id: Long,
    val episodeCount: Int,
    val watchedEpisodeCount: Int
)
