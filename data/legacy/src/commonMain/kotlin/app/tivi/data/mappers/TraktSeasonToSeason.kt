// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktSeason
import app.tivi.data.models.Season
import me.tatarka.inject.annotations.Inject

@Inject
class TraktSeasonToSeason : Mapper<TraktSeason, Season> {

    override fun map(from: TraktSeason) = Season(
        showId = 0,
        traktId = from.ids?.trakt,
        tmdbId = from.ids?.tmdb,
        number = from.number,
        title = from.title,
        summary = from.overview,
        traktRating = from.rating?.toFloat() ?: 0f,
        traktRatingVotes = from.votes ?: 0,
        episodeCount = from.episodeCount,
        episodesAired = from.airedEpisodes,
        network = from.network,
    )
}
