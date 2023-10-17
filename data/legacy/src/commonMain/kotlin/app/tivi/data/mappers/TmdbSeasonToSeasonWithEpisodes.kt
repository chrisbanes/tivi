// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.tmdb.model.TmdbSeason
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbSeasonToSeasonWithEpisodes(
  private val seasonMapper: TmdbSeasonToSeason,
  private val episoderMapper: TmdbEpisodeToEpisode,
) : Mapper<TmdbSeason, Pair<Season, List<Episode>>> {
  override fun map(from: TmdbSeason): Pair<Season, List<Episode>> = Pair(
    seasonMapper.map(from),
    from.episodes?.map { episoderMapper.map(it) } ?: emptyList(),
  )
}
