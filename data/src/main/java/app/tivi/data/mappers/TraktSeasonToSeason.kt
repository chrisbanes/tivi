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

import app.tivi.data.entities.Season
import javax.inject.Inject
import javax.inject.Singleton
import com.uwetrottmann.trakt5.entities.Season as TraktSeason

@Singleton
class TraktSeasonToSeason @Inject constructor() : Mapper<TraktSeason, Season> {
    override suspend fun map(from: TraktSeason) = Season(
            showId = 0,
            traktId = from.ids.trakt,
            tmdbId = from.ids.tmdb,
            number = from.number,
            title = from.title,
            summary = from.overview,
            traktRating = from.rating?.toFloat() ?: 0f,
            traktRatingVotes = from.votes ?: 0,
            episodeCount = from.episode_count,
            episodesAired = from.aired_episodes,
            network = from.network
    )
}