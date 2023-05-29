// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.followedshows

import app.moviebase.trakt.model.TraktList
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.TiviShow

interface FollowedShowsDataSource {
    suspend fun getListShows(listId: Int): List<Pair<FollowedShowEntry, TiviShow>>
    suspend fun addShowIdsToList(listId: Int, shows: List<TiviShow>)
    suspend fun removeShowIdsFromList(listId: Int, shows: List<TiviShow>)
    suspend fun getFollowedListId(): TraktList
}
