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

package app.tivi.data.mappers

import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import javax.inject.Inject
import javax.inject.Singleton
import com.uwetrottmann.trakt5.entities.Season as TraktSeason

@Singleton
class TraktSeasonToSeasonWithEpisodes @Inject constructor(
    private val seasonMapper: TraktSeasonToSeason,
    private val episoderMapper: TraktEpisodeToEpisode
) : Mapper<TraktSeason, Pair<Season, List<Episode>>> {
    override suspend fun map(from: TraktSeason): Pair<Season, List<Episode>> {
        return seasonMapper.map(from) to from.episodes.map { episoderMapper.map(it) }
    }
}