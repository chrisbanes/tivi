// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.trendingshows

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktShowsApi
import app.tivi.data.mappers.TraktTrendingShowToTiviShow
import app.tivi.data.mappers.TraktTrendingShowToTrendingShowEntry
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.TiviShow
import app.tivi.data.models.TrendingShowEntry
import me.tatarka.inject.annotations.Inject

@Inject
class TraktTrendingShowsDataSource(
    private val showsApi: Lazy<TraktShowsApi>,
    showMapper: TraktTrendingShowToTiviShow,
    entryMapper: TraktTrendingShowToTrendingShowEntry,
) : TrendingShowsDataSource {

    private val responseMapper = pairMapperOf(showMapper, entryMapper)

    override suspend operator fun invoke(
        page: Int,
        pageSize: Int,
    ): List<Pair<TiviShow, TrendingShowEntry>> =
        showsApi.value.getTrending(
            // We add 1 because Trakt uses a 1-based index whereas we use a 0-based index
            page = page + 1,
            limit = pageSize,
            extended = TraktExtended.NO_SEASONS,
        ).let { responseMapper(it) }
}
