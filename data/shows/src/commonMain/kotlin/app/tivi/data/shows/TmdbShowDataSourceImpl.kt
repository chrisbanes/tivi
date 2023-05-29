// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.shows

import app.moviebase.tmdb.Tmdb3
import app.tivi.data.mappers.TmdbShowDetailToTiviShow
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbShowDataSourceImpl(
    private val tmdb: Tmdb3,
    private val mapper: TmdbShowDetailToTiviShow,
) : ShowDataSource {
    override suspend fun getShow(show: TiviShow): TiviShow {
        val tmdbId = show.tmdbId
            ?: throw IllegalArgumentException("TmdbId for show does not exist [$show]")

        return tmdb.show.getDetails(tmdbId).let { mapper.map(it) }
    }
}
