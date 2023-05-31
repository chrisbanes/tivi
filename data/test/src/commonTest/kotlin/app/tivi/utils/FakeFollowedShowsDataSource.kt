// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.utils

import app.moviebase.trakt.model.TraktList
import app.tivi.data.followedshows.FollowedShowsDataSource
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.TiviShow

class FakeFollowedShowsDataSource : FollowedShowsDataSource {
    var getListShowsResult: Result<List<Pair<FollowedShowEntry, TiviShow>>> = Result.success(emptyList())
    var addShowIdsToListResult: Result<Unit> = Result.success(Unit)
    var removeShowIdsFromListResult: Result<Unit> = Result.success(Unit)
    var getFollowedListIdResult: Result<TraktList> = Result.success(TraktList())

    override suspend fun getListShows(listId: Int): List<Pair<FollowedShowEntry, TiviShow>> {
        return getListShowsResult.getOrThrow()
    }

    override suspend fun addShowIdsToList(listId: Int, shows: List<TiviShow>) {
        addShowIdsToListResult.getOrThrow()
    }

    override suspend fun removeShowIdsFromList(listId: Int, shows: List<TiviShow>) {
        removeShowIdsFromListResult.getOrThrow()
    }

    override suspend fun getFollowedListId(): TraktList {
        return getFollowedListIdResult.getOrThrow()
    }
}
