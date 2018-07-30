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
import app.tivi.data.entities.Season
import app.tivi.data.mappers.ShowIdToTraktIdMapper
import app.tivi.data.mappers.TraktEpisodeToEpisode
import app.tivi.data.mappers.TraktSeasonToSeason
import app.tivi.extensions.fetchBodyWithRetry
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Seasons
import javax.inject.Inject
import javax.inject.Provider

class TraktSeasonsEpisodesDataSource @Inject constructor(
    private val traktIdMapper: ShowIdToTraktIdMapper,
    private val seasonsService: Provider<Seasons>,
    private val seasonMapper: TraktSeasonToSeason,
    private val episodeMapper: TraktEpisodeToEpisode
) : SeasonsEpisodesDataSource {
    override suspend fun getSeasonsEpisodes(showId: Long): List<Pair<Season, List<Episode>>> {
        val traktId = traktIdMapper.map(showId) ?: return emptyList()

        val results = seasonsService.get().summary(traktId.toString(), Extended.FULLEPISODES)
                .fetchBodyWithRetry()

        return results.map { seasonMapper.map(it).copy(showId = showId) to it.episodes.map(episodeMapper::map) }
    }
}