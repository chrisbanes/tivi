/*
 * Copyright 2018 Google, Inc.
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
import app.tivi.data.mappers.ShowIdToTmdbIdMapper
import app.tivi.data.mappers.TmdbEpisodeToEpisode
import app.tivi.extensions.fetchBodyWithRetry
import com.uwetrottmann.tmdb2.Tmdb
import retrofit2.HttpException
import javax.inject.Inject

class TmdbSeasonsEpisodesDataSource @Inject constructor(
    private val tmdbIdMapper: ShowIdToTmdbIdMapper,
    private val tmdb: Tmdb,
    private val episodeMapper: TmdbEpisodeToEpisode
) : EpisodeDataSource {
    override suspend fun getEpisode(showId: Long, seasonNumber: Int, episodeNumber: Int): Episode? {
        val showTmdbId = tmdbIdMapper.map(showId)
                ?: throw IllegalStateException("Show with show id [$showId] does not exist")
        try {
            return tmdb.tvEpisodesService().episode(showTmdbId, seasonNumber, episodeNumber)
                    .fetchBodyWithRetry()
                    .let(episodeMapper::map)
        } catch (e: HttpException) {
            when {
                e.code() == 404 -> return null
                else -> throw e
            }
        }
    }
}