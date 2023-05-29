// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktTrendingShow
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TraktTrendingShowToTiviShow(
    private val showMapper: TraktShowToTiviShow,
) : Mapper<TraktTrendingShow, TiviShow> {
    override fun map(from: TraktTrendingShow) = showMapper.map(from.show!!)
}
