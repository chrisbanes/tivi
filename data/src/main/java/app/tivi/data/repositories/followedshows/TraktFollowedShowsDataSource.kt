/*
 * Copyright 2018 Google LLC
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

import app.tivi.data.entities.ErrorResult
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.Result
import app.tivi.data.entities.Success
import app.tivi.data.entities.TiviShow
import app.tivi.data.mappers.TraktListEntryToFollowedShowEntry
import app.tivi.data.mappers.TraktListEntryToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.extensions.bodyOrThrow
import app.tivi.extensions.executeWithRetry
import app.tivi.extensions.toResult
import app.tivi.extensions.toResultUnit
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
    listEntryToShowMapper: TraktListEntryToTiviShow,
    listEntryToFollowedEntry: TraktListEntryToFollowedShowEntry
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
        return usersService.get().addListItems(UserSlug.ME, listId.toString(), syncItems)
            .executeWithRetry()
            .toResultUnit()
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
        return usersService.get().deleteListItems(UserSlug.ME, listId.toString(), syncItems)
            .executeWithRetry()
            .toResultUnit()
    }

    override suspend fun getListShows(listId: Int): Result<List<Pair<FollowedShowEntry, TiviShow>>> {
        return usersService.get().listItems(UserSlug.ME, listId.toString(), Extended.NOSEASONS)
            .executeWithRetry()
            .toResult(listShowsMapper)
    }

    override suspend fun getFollowedListId(): Result<Int> {
        val fetchResult: Result<Int>? = try {
            usersService.get().lists(UserSlug.ME)
                .executeWithRetry()
                .bodyOrThrow()
                .firstOrNull { it.name == LIST_NAME }
                ?.let { Success(it.ids.trakt) }
        } catch (t: Throwable) {
            ErrorResult(t)
        }

        if (fetchResult is Success) {
            return fetchResult
        }

        return try {
            usersService.get().createList(
                UserSlug.ME,
                TraktList().name(LIST_NAME).privacy(ListPrivacy.PRIVATE)
            )
                .executeWithRetry()
                .bodyOrThrow()
                .let { Success(it.ids.trakt) }
        } catch (t: Throwable) {
            ErrorResult(t)
        }
    }
}
