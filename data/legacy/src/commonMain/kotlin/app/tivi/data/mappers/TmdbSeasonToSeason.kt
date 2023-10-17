// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.tmdb.model.TmdbSeason
import app.tivi.data.models.Season
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbSeasonToSeason : Mapper<TmdbSeason, Season> {
  override fun map(from: TmdbSeason) = Season(
    showId = 0,
    tmdbId = from.id,
    number = from.seasonNumber,
    title = from.name,
    summary = from.overview,
    episodeCount = from.episodeCount,
    tmdbPosterPath = from.posterPath,
  )
}
