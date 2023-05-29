// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.search

import app.moviebase.tmdb.Tmdb3
import app.tivi.data.mappers.TmdbShowPageResultToTiviShows
import app.tivi.data.models.ShowTmdbImage
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbSearchDataSource(
    private val tmdb: Tmdb3,
    private val mapper: TmdbShowPageResultToTiviShows,
) : SearchDataSource {
    override suspend fun search(
        query: String,
    ): List<Pair<TiviShow, List<ShowTmdbImage>>> {
        return tmdb.search
            .findShows(query, 1)
            .let { mapper.map(it) }
    }
}
