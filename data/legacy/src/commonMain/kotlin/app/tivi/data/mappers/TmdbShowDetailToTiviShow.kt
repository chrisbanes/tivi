// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.tmdb.model.TmdbShowDetail
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbShowDetailToTiviShow : Mapper<TmdbShowDetail, TiviShow> {
    override fun map(from: TmdbShowDetail) = TiviShow(
        tmdbId = from.id,
        imdbId = from.externalIds?.imdbId,
        title = from.name,
        summary = from.overview,
        homepage = from.homepage,
        network = from.networks.firstOrNull()?.name,
        networkLogoPath = from.networks.firstOrNull()?.logoPath,
    )
}
