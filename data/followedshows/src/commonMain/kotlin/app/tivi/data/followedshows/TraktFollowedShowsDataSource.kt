// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.followedshows

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktUsersApi
import app.moviebase.trakt.model.TraktItemIds
import app.moviebase.trakt.model.TraktList
import app.moviebase.trakt.model.TraktListPrivacy
import app.moviebase.trakt.model.TraktSyncItems
import app.moviebase.trakt.model.TraktSyncShow
import app.tivi.data.mappers.TraktListItemToFollowedShowEntry
import app.tivi.data.mappers.TraktListItemToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TraktFollowedShowsDataSource(
    usersApi: Lazy<TraktUsersApi>,
    listEntryToShowMapper: TraktListItemToTiviShow,
    listEntryToFollowedEntry: TraktListItemToFollowedShowEntry,
) : FollowedShowsDataSource {
    private companion object {
        const val LIST_NAME = "Following"
        val FOLLOWED_LIST = TraktList(name = LIST_NAME, privacy = TraktListPrivacy.PRIVATE)
    }

    private val listShowsMapper = pairMapperOf(listEntryToFollowedEntry, listEntryToShowMapper)
    private val usersApi by usersApi

    override suspend fun addShowIdsToList(listId: Int, shows: List<TiviShow>) {
        usersApi.addListItems(listId = listId.toString(), items = shows.toSyncItems())
    }

    override suspend fun removeShowIdsFromList(listId: Int, shows: List<TiviShow>) {
        usersApi.removeListItems(listId = listId.toString(), items = shows.toSyncItems())
    }

    override suspend fun getListShows(listId: Int): List<Pair<FollowedShowEntry, TiviShow>> {
        return usersApi.getListItems(listId = listId.toString(), extended = TraktExtended.NO_SEASONS)
            .filter { it.show != null }
            .let { listShowsMapper.invoke(it) }
    }

    override suspend fun getFollowedListId(): TraktList =
        usersApi.getLists().find { it.name == LIST_NAME }
            ?: usersApi.createList(list = FOLLOWED_LIST)

    private fun List<TiviShow>.toSyncItems() = TraktSyncItems(shows = map { it.toSyncItem() })

    private fun TiviShow.toSyncItem() = TraktSyncShow(
        ids = TraktItemIds(trakt = traktId, imdb = imdbId, tmdb = tmdbId),
    )
}
