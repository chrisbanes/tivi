// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.tmdb.model.TmdbEpisode
import app.tivi.data.models.Episode
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbEpisodeToEpisode : Mapper<TmdbEpisode, Episode> {
  override fun map(from: TmdbEpisode): Episode = Episode(
    seasonId = 0,
    tmdbId = from.id,
    title = from.name,
    number = from.episodeNumber,
    summary = from.overview,
    tmdbBackdropPath = from.stillPath,
  )
}
