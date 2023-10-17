// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.tmdb.model.TmdbSeasonDetail
import app.tivi.data.models.Season
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbSeasonDetailToSeason : Mapper<TmdbSeasonDetail, Season> {
  override fun map(from: TmdbSeasonDetail): Season = Season(
    showId = 0,
    tmdbId = from.id,
    number = from.seasonNumber,
    title = from.name,
    summary = from.overview,
    episodeCount = from.episodeCount,
    tmdbPosterPath = from.posterPath,
  )
}
