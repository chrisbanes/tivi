// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes

import app.moviebase.trakt.api.TraktEpisodesApi
import app.tivi.data.mappers.ShowIdToTraktOrImdbIdMapper
import app.tivi.data.mappers.TraktEpisodeToEpisode
import app.tivi.data.models.Episode
import me.tatarka.inject.annotations.Inject

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
        val id = idMapper.map(showId) ?: error("No Trakt allowed ID for show with ID: $showId")

        return service.value
            .getSummary(id, seasonNumber, episodeNumber)
            .let { episodeMapper.map(it) }
    }
}
