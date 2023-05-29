// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktShowsApi
import app.moviebase.trakt.model.TraktShow
import app.tivi.data.mappers.IndexedMapper
import app.tivi.data.mappers.ShowIdToTraktOrImdbIdMapper
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TraktRelatedShowsDataSourceImpl(
    private val idMapper: ShowIdToTraktOrImdbIdMapper,
    private val showService: Lazy<TraktShowsApi>,
    showMapper: TraktShowToTiviShow,
) : TraktRelatedShowsDataSource {
    private val entryMapper = IndexedMapper<TraktShow, RelatedShowEntry> { index, _ ->
        RelatedShowEntry(showId = 0, otherShowId = 0, orderIndex = index)
    }
    private val resultMapper = pairMapperOf(showMapper, entryMapper)

    override suspend operator fun invoke(showId: Long): List<Pair<TiviShow, RelatedShowEntry>> {
        val id = idMapper.map(showId) ?: error("No Trakt allowed ID for show with ID: $showId")

        return showService.value
            .getRelated(id, 0, 10, TraktExtended.NO_SEASONS)
            .let { resultMapper(it) }
    }
}
