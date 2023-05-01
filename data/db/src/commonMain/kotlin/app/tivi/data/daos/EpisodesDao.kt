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

package app.tivi.data.daos

import app.tivi.data.compoundmodels.EpisodeWithSeason
import app.tivi.data.models.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodesDao : EntityDao<Episode> {

    suspend fun episodesWithSeasonId(seasonId: Long): List<Episode>

    suspend fun deleteWithSeasonId(seasonId: Long)

    suspend fun episodeWithTraktId(traktId: Int): Episode?

    suspend fun episodeWithTmdbId(tmdbId: Int): Episode?

    suspend fun episodeWithId(id: Long): Episode?

    suspend fun episodeTraktIdForId(id: Long): Int?

    suspend fun episodeIdWithTraktId(traktId: Int): Long?

    fun episodeWithIdObservable(id: Long): Flow<EpisodeWithSeason>

    suspend fun showIdForEpisodeId(episodeId: Long): Long

    fun observeLatestWatchedEpisodeForShowId(showId: Long): Flow<EpisodeWithSeason?>

    fun observeNextEpisodeForShowAfter(
        showId: Long,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Flow<EpisodeWithSeason?>

    fun observeNextAiredEpisodeForShowAfter(
        showId: Long,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Flow<EpisodeWithSeason?>
}
