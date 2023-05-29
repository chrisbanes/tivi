// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktMediaItem
import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import me.tatarka.inject.annotations.Inject

@Inject
class TraktBaseShowToWatchedShowEntry(
    private val showMapper: TraktShowToTiviShow,
) : Mapper<TraktMediaItem, Pair<TiviShow, WatchedShowEntry>> {

    override fun map(from: TraktMediaItem): Pair<TiviShow, WatchedShowEntry> {
        val watchedShowEntry = WatchedShowEntry(
            showId = 0,
            lastWatched = from.lastWatchedAt!!,
            lastUpdated = from.lastUpdatedAt!!,
        )
        return showMapper.map(from.show!!) to watchedShowEntry
    }
}
