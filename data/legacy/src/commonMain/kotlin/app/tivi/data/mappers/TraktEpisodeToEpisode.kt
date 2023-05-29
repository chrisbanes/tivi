// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktEpisode
import app.tivi.data.models.Episode
import me.tatarka.inject.annotations.Inject

@Inject
class TraktEpisodeToEpisode : Mapper<TraktEpisode, Episode> {

    override fun map(from: TraktEpisode) = Episode(
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
