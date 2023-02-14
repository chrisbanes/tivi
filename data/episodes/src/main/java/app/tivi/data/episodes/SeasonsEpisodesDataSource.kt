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

package app.tivi.data.episodes

import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.Season
import kotlinx.datetime.Instant

interface SeasonsEpisodesDataSource {
    suspend fun getSeasonsEpisodes(showId: Long): List<Pair<Season, List<Episode>>>
    suspend fun getShowEpisodeWatches(
        showId: Long,
        since: Instant? = null,
    ): List<Pair<Episode, EpisodeWatchEntry>>

    suspend fun getEpisodeWatches(
        episodeId: Long,
        since: Instant? = null,
    ): List<EpisodeWatchEntry>

    suspend fun getSeasonWatches(
        seasonId: Long,
        since: Instant? = null,
    ): List<Pair<Episode, EpisodeWatchEntry>>

    suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>)
    suspend fun removeEpisodeWatches(watches: List<EpisodeWatchEntry>)
}
