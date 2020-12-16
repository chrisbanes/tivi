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
import org.threeten.bp.OffsetDateTime

@DatabaseView(
    value =
    """
SELECT
  fs.id,
  MIN(datetime(eps.first_aired)) AS next_ep_to_watch_air_date
FROM
  myshows_entries as fs
  INNER JOIN seasons AS s ON fs.show_id = s.show_id
  INNER JOIN episodes AS eps ON eps.season_id = s.id
  LEFT JOIN episode_watch_entries as ew ON ew.episode_id = eps.id
  INNER JOIN followed_last_watched_airdate AS lw ON lw.id = fs.id
WHERE
  s.number != ${Season.NUMBER_SPECIALS}
  AND s.ignored = 0
  AND watched_at IS NULL
  AND datetime(first_aired) < datetime('now')
  AND datetime(first_aired) > datetime(last_watched_air_date)
GROUP BY
  fs.id
""",
    viewName = "followed_next_to_watch"
)
data class FollowedShowsNextToWatch(
    val id: Long,
    val nextEpisodeToWatchAirDate: OffsetDateTime?
)
