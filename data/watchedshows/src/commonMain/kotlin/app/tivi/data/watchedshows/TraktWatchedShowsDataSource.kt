// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.watchedshows

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktSyncApi
import app.tivi.data.mappers.TraktBaseShowToWatchedShowEntry
import app.tivi.data.mappers.map
import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import me.tatarka.inject.annotations.Inject

@Inject
class TraktWatchedShowsDataSource(
    private val syncApi: Lazy<TraktSyncApi>,
    private val mapper: TraktBaseShowToWatchedShowEntry,
) : WatchedShowsDataSource {

    override suspend operator fun invoke(): List<Pair<TiviShow, WatchedShowEntry>> =
        syncApi.value.getWatchedShows(extended = TraktExtended.NO_SEASONS).let { mapper.map(it) }
}
