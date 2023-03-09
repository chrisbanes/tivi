/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    private val usersApi: Lazy<TraktUsersApi>,
    listEntryToShowMapper: TraktListItemToTiviShow,
    listEntryToFollowedEntry: TraktListItemToFollowedShowEntry,
) : FollowedShowsDataSource {
    companion object {
        private const val LIST_NAME = "Following"
    }

    private val listShowsMapper = pairMapperOf(listEntryToFollowedEntry, listEntryToShowMapper)

    override suspend fun addShowIdsToList(listId: Int, shows: List<TiviShow>) {
        usersApi.value.addListItems(listId = listId.toString(), items = shows.toSyncItems())
    }

    override suspend fun removeShowIdsFromList(listId: Int, shows: List<TiviShow>) {
        usersApi.value.removeListItems(listId = listId.toString(), items = shows.toSyncItems())
    }

    override suspend fun getListShows(listId: Int): List<Pair<FollowedShowEntry, TiviShow>> =
        usersApi.value
            .getListItems(listId = listId.toString(), extended = TraktExtended.NOSEASONS)
            .let { listShowsMapper.invoke(it) }

    override suspend fun getFollowedListId(): TraktList =
        usersApi.value.getLists().find { it.name == LIST_NAME }
            ?: usersApi.value.createList(
                list = TraktList(name = LIST_NAME, privacy = TraktListPrivacy.PRIVATE),
            )

    private fun List<TiviShow>.toSyncItems() = TraktSyncItems(shows = map { it.toSyncItem() })

    private fun TiviShow.toSyncItem() = TraktSyncShow(
        ids = TraktItemIds(trakt = traktId, imdb = imdbId, tmdb = tmdbId),
    )
}
