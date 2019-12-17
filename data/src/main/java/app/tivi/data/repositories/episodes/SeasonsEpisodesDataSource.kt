/*
 * Copyright 2018 Google LLC
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

package app.tivi.data.repositories.episodes

import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.Result
import app.tivi.data.entities.Season
import org.threeten.bp.OffsetDateTime

interface SeasonsEpisodesDataSource {
    suspend fun getSeasonsEpisodes(showId: Long): Result<List<Pair<Season, List<Episode>>>>
    suspend fun getShowEpisodeWatches(
        showId: Long,
        since: OffsetDateTime? = null
    ): Result<List<Pair<Episode, EpisodeWatchEntry>>>

    suspend fun getEpisodeWatches(
        episodeId: Long,
        since: OffsetDateTime? = null
    ): Result<List<EpisodeWatchEntry>>

    suspend fun getSeasonWatches(
        seasonId: Long,
        since: OffsetDateTime? = null
    ): Result<List<Pair<Episode, EpisodeWatchEntry>>>

    suspend fun addEpisodeWatches(watches: List<EpisodeWatchEntry>): Result<Unit>
    suspend fun removeEpisodeWatches(watches: List<EpisodeWatchEntry>): Result<Unit>
}
