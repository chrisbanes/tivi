// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes.datasource

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktSeasonsApi
import app.tivi.data.mappers.ShowIdToTraktOrImdbIdMapper
import app.tivi.data.mappers.TraktSeasonToSeasonWithEpisodes
import app.tivi.data.mappers.map
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import me.tatarka.inject.annotations.Inject

interface SeasonsEpisodesDataSource {
    suspend fun getSeasonsEpisodes(showId: Long): List<Pair<Season, List<Episode>>>
}

@Inject
class TraktSeasonsEpisodesDataSource(
    private val showIdToAnyIdMapper: ShowIdToTraktOrImdbIdMapper,
    private val seasonsService: Lazy<TraktSeasonsApi>,
    private val seasonMapper: TraktSeasonToSeasonWithEpisodes,
) : SeasonsEpisodesDataSource {
    override suspend fun getSeasonsEpisodes(
        showId: Long,
    ): List<Pair<Season, List<Episode>>> {
        return seasonsService.value.getSummary(
            showId = showIdToAnyIdMapper.map(showId)
                ?: error("No Trakt ID for show with ID: $showId"),
            extended = TraktExtended.FULL_EPISODES,
        ).let(seasonMapper::map)
    }
}
