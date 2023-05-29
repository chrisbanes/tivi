// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.popularshows

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktShowsApi
import app.moviebase.trakt.model.TraktShow
import app.tivi.data.mappers.IndexedMapper
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.PopularShowEntry
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TraktPopularShowsDataSource(
    private val showService: Lazy<TraktShowsApi>,
    showMapper: TraktShowToTiviShow,
) : PopularShowsDataSource {
    private val entryMapper = IndexedMapper<TraktShow, PopularShowEntry> { index, _ ->
        PopularShowEntry(showId = 0, pageOrder = index, page = 0)
    }

    private val resultsMapper = pairMapperOf(showMapper, entryMapper)

    override suspend operator fun invoke(
        page: Int,
        pageSize: Int,
    ): List<Pair<TiviShow, PopularShowEntry>> =
        showService.value
            .getPopular(page = page + 1, limit = pageSize, extended = TraktExtended.NO_SEASONS)
            .let { resultsMapper(it) }
}
