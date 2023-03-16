/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
