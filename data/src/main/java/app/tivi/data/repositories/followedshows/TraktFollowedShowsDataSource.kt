/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.data.repositories.followedshows

import app.tivi.data.RetrofitRunner
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.Result
import app.tivi.data.entities.TiviShow
import app.tivi.data.mappers.TraktListEntryToFollowedShowEntry
import app.tivi.data.mappers.TraktListEntryToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.extensions.executeWithRetry
import app.tivi.extensions.fetchBodyWithRetry
import com.uwetrottmann.trakt5.entities.ShowIds
import com.uwetrottmann.trakt5.entities.SyncItems
import com.uwetrottmann.trakt5.entities.SyncShow
import com.uwetrottmann.trakt5.entities.TraktList
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.enums.ListPrivacy
import com.uwetrottmann.trakt5.services.Users
import javax.inject.Inject
import javax.inject.Provider

class TraktFollowedShowsDataSource @Inject constructor(
    private val usersService: Provider<Users>,
    private val retrofitRunner: RetrofitRunner,
    private val listEntryToShowMapper: TraktListEntryToTiviShow,
    private val listEntryToFollowedEntry: TraktListEntryToFollowedShowEntry
) : FollowedShowsDataSource {
    companion object {
        private val LIST_NAME = "Following"
    }
    private val listShowsMapper = pairMapperOf(listEntryToFollowedEntry, listEntryToShowMapper)

    override suspend fun addShowIdsToList(listId: Int, shows: List<TiviShow>): Result<Unit> {
        val syncItems = SyncItems()
        syncItems.shows = shows.map { show ->
            SyncShow().apply {
                ids = ShowIds().apply {
                    trakt = show.traktId
                    imdb = show.imdbId
                    tmdb = show.tmdbId
                }
            }
        }
        return retrofitRunner.executeForResponse {
            usersService.get().addListItems(UserSlug.ME, listId.toString(), syncItems).executeWithRetry()
        }
    }

    override suspend fun removeShowIdsFromList(listId: Int, shows: List<TiviShow>): Result<Unit> {
        val syncItems = SyncItems()
        syncItems.shows = shows.map { show ->
            SyncShow().apply {
                ids = ShowIds().apply {
                    trakt = show.traktId
                    imdb = show.imdbId
                    tmdb = show.tmdbId
                }
            }
        }
        return retrofitRunner.executeForResponse {
            usersService.get().deleteListItems(UserSlug.ME, listId.toString(), syncItems).executeWithRetry()
        }
    }

    override suspend fun getListShows(listId: Int): Result<List<Pair<FollowedShowEntry, TiviShow>>> {
        return retrofitRunner.executeForResponse(listShowsMapper) {
            usersService.get().listItems(UserSlug.ME, listId.toString(), Extended.NOSEASONS)
                    .executeWithRetry()
        }
    }

    override suspend fun getFollowedListId(): Int {
        val list = usersService.get().lists(UserSlug.ME).fetchBodyWithRetry()
                .first { it.name == LIST_NAME }

        return if (list != null) {
            list.ids.trakt
        } else {
            usersService.get().createList(
                    UserSlug.ME,
                    TraktList().name(LIST_NAME).privacy(ListPrivacy.PRIVATE)
            ).fetchBodyWithRetry().let {
                it.ids.trakt
            }
        }
    }
}