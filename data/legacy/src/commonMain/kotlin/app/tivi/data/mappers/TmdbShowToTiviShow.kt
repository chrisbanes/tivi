// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.tmdb.model.TmdbShow
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbShowToTiviShow : Mapper<TmdbShow, TiviShow> {
    override fun map(from: TmdbShow) = TiviShow(
        tmdbId = from.id,
        title = from.name,
        summary = from.overview,
    )
}
