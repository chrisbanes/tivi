// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes.datasource

import app.moviebase.tmdb.Tmdb3
import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktSeasonsApi
import app.tivi.data.mappers.ShowIdToTmdbIdMapper
import app.tivi.data.mappers.ShowIdToTraktOrImdbIdMapper
import app.tivi.data.mappers.TmdbSeasonDetailToSeason
import app.tivi.data.mappers.TmdbSeasonToSeasonWithEpisodes
import app.tivi.data.mappers.TraktSeasonToSeasonWithEpisodes
import app.tivi.data.mappers.map
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import me.tatarka.inject.annotations.Inject

interface SeasonsEpisodesDataSource {
  suspend fun getSeason(showId: Long, seasonNumber: Int): Season?
  suspend fun getSeasonsEpisodes(showId: Long): List<Pair<Season, List<Episode>>>
}

@Inject
class TraktSeasonsEpisodesDataSourceImpl(
  private val showIdToAnyIdMapper: ShowIdToTraktOrImdbIdMapper,
  private val seasonsService: Lazy<TraktSeasonsApi>,
  private val seasonMapper: TraktSeasonToSeasonWithEpisodes,
) : SeasonsEpisodesDataSource {
  override suspend fun getSeason(showId: Long, seasonNumber: Int): Season? {
    // Trakt API doesn't currently support this
    return null
  }

  override suspend fun getSeasonsEpisodes(
    showId: Long,
  ): List<Pair<Season, List<Episode>>> {
    return seasonsService.value.getSummary(
      showId = requireNotNull(showIdToAnyIdMapper.map(showId)) {
        "No Trakt ID for show with ID: $showId"
      },
      extended = TraktExtended.FULL_EPISODES,
    ).let(seasonMapper::map)
  }
}

@Inject
class TmdbSeasonsEpisodesDataSourceImpl(
  private val showIdToTmdbIdMapper: ShowIdToTmdbIdMapper,
  private val tmdb: Tmdb3,
  private val seasonMapper: TmdbSeasonDetailToSeason,
  private val seasonWithEpsMapper: TmdbSeasonToSeasonWithEpisodes,
) : SeasonsEpisodesDataSource {

  override suspend fun getSeason(showId: Long, seasonNumber: Int): Season? {
    return tmdb.showSeasons.getDetails(
      showId = showIdToTmdbIdMapper.map(showId)
        ?: error("No TMDb ID for show with ID: $showId"),
      seasonNumber = seasonNumber,
    ).let(seasonMapper::map)
  }

  override suspend fun getSeasonsEpisodes(
    showId: Long,
  ): List<Pair<Season, List<Episode>>> {
    val show = tmdb.show.getDetails(
      showId = showIdToTmdbIdMapper.map(showId)
        ?: error("No TMDb ID for show with ID: $showId"),
    )
    return show.seasons.map(seasonWithEpsMapper::map)
  }
}
