// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbShow
import app.tivi.data.mappers.IndexedMapper
import app.tivi.data.mappers.ShowIdToTmdbIdMapper
import app.tivi.data.mappers.TmdbShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbRelatedShowsDataSourceImpl(
    private val tmdbIdMapper: ShowIdToTmdbIdMapper,
    private val tmdb: Tmdb3,
    showMapper: TmdbShowToTiviShow,
) : TmdbRelatedShowsDataSource {

    private val entryMapper = IndexedMapper<TmdbShow, RelatedShowEntry> { index, _ ->
        RelatedShowEntry(showId = 0, otherShowId = 0, orderIndex = index)
    }
    private val resultMapper = pairMapperOf(showMapper, entryMapper)

    override suspend operator fun invoke(
        showId: Long,
    ): List<Pair<TiviShow, RelatedShowEntry>> {
        val tmdbShowId = tmdbIdMapper.map(showId)
        require(tmdbShowId != null) { "No Tmdb ID for show with ID: $showId" }

        return tmdb.show
            .getRecommendations(tmdbShowId, 1, null)
            .let { resultMapper(it.results) }
    }
}
