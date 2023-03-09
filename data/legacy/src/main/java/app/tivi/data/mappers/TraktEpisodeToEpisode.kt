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

import app.moviebase.trakt.model.TraktEpisode
import app.tivi.data.models.Episode
import me.tatarka.inject.annotations.Inject

@Inject
class TraktEpisodeToEpisode() : Mapper<TraktEpisode, Episode> {

    override suspend fun map(from: TraktEpisode) = Episode(
        seasonId = 0,
        traktId = from.ids?.trakt,
        tmdbId = from.ids?.tmdb,
        title = from.title,
        number = from.number,
        summary = from.overview,
        firstAired = from.firstAired,
        traktRating = from.rating?.toFloat() ?: 0f,
        traktRatingVotes = from.votes,
    )
}
