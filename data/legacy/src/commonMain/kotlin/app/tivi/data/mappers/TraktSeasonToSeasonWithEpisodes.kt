// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktSeason
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import me.tatarka.inject.annotations.Inject

@Inject
class TraktSeasonToSeasonWithEpisodes(
    private val seasonMapper: TraktSeasonToSeason,
    private val episoderMapper: TraktEpisodeToEpisode,
) : Mapper<TraktSeason, Pair<Season, List<Episode>>> {

    override fun map(from: TraktSeason): Pair<Season, List<Episode>> = Pair(
        seasonMapper.map(from),
        from.episodes?.map { episoderMapper.map(it) } ?: emptyList(),
    )
}
