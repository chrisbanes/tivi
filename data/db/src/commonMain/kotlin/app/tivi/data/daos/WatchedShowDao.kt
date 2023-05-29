// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.paging.PagingSource
import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.compoundmodels.WatchedShowEntryWithShow
import app.tivi.data.models.SortOption
import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.views.ShowsWatchStats
import kotlinx.coroutines.flow.Flow

interface WatchedShowDao : EntryDao<WatchedShowEntry, WatchedShowEntryWithShow> {

    fun entryWithShowId(showId: Long): WatchedShowEntry?

    fun entries(): List<WatchedShowEntry>

    fun entriesObservable(): Flow<List<WatchedShowEntry>>

    override fun deleteAll()

    fun pagedUpNextShows(followedOnly: Boolean = false, sort: SortOption): PagingSource<Int, UpNextEntry>

    fun getUpNextShows(): List<UpNextEntry>

    fun entryShowViewStats(showId: Long): Flow<ShowsWatchStats?>

    fun observeNextShowToWatch(): Flow<TiviShow?>
}
