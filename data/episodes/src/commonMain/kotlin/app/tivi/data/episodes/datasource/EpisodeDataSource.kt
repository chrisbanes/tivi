// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes.datasource

import app.moviebase.tmdb.Tmdb3
import app.moviebase.trakt.api.TraktEpisodesApi
import app.tivi.data.mappers.ShowIdToTmdbIdMapper
import app.tivi.data.mappers.ShowIdToTraktOrImdbIdMapper
import app.tivi.data.mappers.TmdbEpisodeDetailToEpisode
import app.tivi.data.mappers.TraktEpisodeToEpisode
import app.tivi.data.models.Episode
import me.tatarka.inject.annotations.Inject

interface EpisodeDataSource {
  suspend fun getEpisode(showId: Long, seasonNumber: Int, episodeNumber: Int): Episode
}

@Inject
class TraktEpisodeDataSourceImpl(
  private val idMapper: ShowIdToTraktOrImdbIdMapper,
  private val service: Lazy<TraktEpisodesApi>,
  private val episodeMapper: TraktEpisodeToEpisode,
) : EpisodeDataSource {

  override suspend fun getEpisode(
    showId: Long,
    seasonNumber: Int,
    episodeNumber: Int,
  ): Episode {
    val id = requireNotNull(idMapper.map(showId)) {
      "No Trakt ID for show with ID: $showId"
    }

    return service.value
      .getSummary(id, seasonNumber, episodeNumber)
      .let { episodeMapper.map(it) }
  }
}

@Inject
class TmdbEpisodeDataSourceImpl(
  private val tmdbIdMapper: ShowIdToTmdbIdMapper,
  private val tmdb: Tmdb3,
  private val episodeMapper: TmdbEpisodeDetailToEpisode,
) : EpisodeDataSource {
  override suspend fun getEpisode(
    showId: Long,
    seasonNumber: Int,
    episodeNumber: Int,
  ): Episode {
    val tmdbShowId = tmdbIdMapper.map(showId)
    require(tmdbShowId != null) { "No Tmdb ID for show with ID: $showId" }

    return tmdb.showEpisodes.getDetails(
      showId = tmdbShowId,
      seasonNumber = seasonNumber,
      episodeNumber = episodeNumber,
    ).let { episodeMapper.map(it) }
  }
}
