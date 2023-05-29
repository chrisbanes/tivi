// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktTrendingShow
import app.tivi.data.models.TrendingShowEntry
import me.tatarka.inject.annotations.Inject

@Inject
class TraktTrendingShowToTrendingShowEntry : Mapper<TraktTrendingShow, TrendingShowEntry> {

    override fun map(from: TraktTrendingShow): TrendingShowEntry {
        return TrendingShowEntry(
            showId = 0,
            watchers = from.watchers ?: 0,
            page = 0,
        )
    }
}
