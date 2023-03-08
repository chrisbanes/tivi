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

import app.moviebase.tmdb.Tmdb3
import app.tivi.data.mappers.ShowIdToTmdbIdMapper
import app.tivi.data.mappers.TmdbEpisodeDetailToEpisode
import app.tivi.data.models.Episode
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbEpisodeDataSourceImpl(
    private val tmdbIdMapper: ShowIdToTmdbIdMapper,
    private val tmdb: Tmdb3,
    private val episodeMapper: TmdbEpisodeDetailToEpisode,
) : EpisodeDataSource {
    override suspend fun getEpisode(
        showId: Long,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Episode {
        return tmdb.showEpisodes
            .getDetails(
                showId = tmdbIdMapper.map(showId),
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
            )
            .let { episodeMapper.map(it) }
    }
}
