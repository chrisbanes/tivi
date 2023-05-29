// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.shows

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktSearchApi
import app.moviebase.trakt.api.TraktShowsApi
import app.moviebase.trakt.model.TraktIdType
import app.moviebase.trakt.model.TraktSearchQuery
import app.moviebase.trakt.model.TraktSearchType
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TraktShowDataSourceImpl(
    private val showService: Lazy<TraktShowsApi>,
    private val searchService: Lazy<TraktSearchApi>,
    private val mapper: TraktShowToTiviShow,
) : ShowDataSource {
    override suspend fun getShow(show: TiviShow): TiviShow {
        var traktId = show.traktId

        if (traktId == null && show.tmdbId != null) {
            // We need to fetch the search for the trakt id
            traktId = searchService.value
                .searchIdLookup(
                    idType = TraktIdType.TMDB,
                    id = show.tmdbId.toString(),
                    searchType = TraktSearchType.SHOW,
                    extended = TraktExtended.NO_SEASONS,
                    page = 1,
                    limit = 1,
                ).getOrNull(0)?.show?.ids?.trakt
        }

        if (traktId == null) {
            val searchQuery = TraktSearchQuery(
                query = show.title,
                countries = show.country,
                networks = show.network,
                extended = TraktExtended.NO_SEASONS,
                page = 1,
                limit = 1,
            )
            traktId = searchService.value
                .searchTextQueryShow(searchQuery)
                .firstOrNull()?.show?.ids?.trakt
        }

        return if (traktId != null) {
            showService.value
                .getSummary(showId = traktId.toString(), extended = TraktExtended.FULL)
                .let { mapper.map(it) }
        } else {
            throw IllegalArgumentException("Trakt ID for show does not exist: [$show]")
        }
    }
}
