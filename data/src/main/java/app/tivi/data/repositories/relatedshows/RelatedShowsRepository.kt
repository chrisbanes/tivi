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

package app.tivi.data.repositories.relatedshows

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.RelatedShowEntry
import app.tivi.data.entities.Success
import app.tivi.data.entities.TiviShow
import app.tivi.data.fetch
import app.tivi.data.fetchCollection
import app.tivi.data.instantInPast
import app.tivi.data.repositories.shows.ShowImagesStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.extensions.asyncOrAwait
import app.tivi.extensions.parallelForEach
import javax.inject.Inject
import javax.inject.Singleton
import org.threeten.bp.Instant

@Singleton
class RelatedShowsRepository @Inject constructor(
    private val relatedShowsStore: RelatedShowsStore,
    private val lastRequestStore: RelatedShowsLastRequestStore,
    private val traktDataSource: TraktRelatedShowsDataSource,
    private val tmdbDataSource: TmdbRelatedShowsDataSource,
    private val showDao: TiviShowDao,
    private val showStore: ShowStore,
    private val showImagesStore: ShowImagesStore
) {
    fun observeRelatedShows(showId: Long) = relatedShowsStore.observeRelatedShows(showId)

    suspend fun getRelatedShows(showId: Long) = relatedShowsStore.getRelatedShows(showId)

    suspend fun needUpdate(showId: Long, expiry: Instant = instantInPast(days = 28)): Boolean {
        return lastRequestStore.isRequestBefore(showId, expiry)
    }

    suspend fun updateRelatedShows(showId: Long) {
        asyncOrAwait("update_related_shows_$showId") {
            val tmdbResults = tmdbDataSource.getRelatedShows(showId)
            if (tmdbResults is Success && tmdbResults.data.isNotEmpty()) {
                process(showId, tmdbResults.data)
                lastRequestStore.updateLastRequest(showId)
                return@asyncOrAwait
            }

            val traktResults = traktDataSource.getRelatedShows(showId)
            if (traktResults is Success && traktResults.data.isNotEmpty()) {
                process(showId, traktResults.data)
                lastRequestStore.updateLastRequest(showId)
                return@asyncOrAwait
            }
        }
    }

    private suspend fun process(showId: Long, list: List<Pair<TiviShow, RelatedShowEntry>>) {
        list.map { (show, entry) ->
            // Grab the show id if it exists, or save the show and use it's generated ID
            val relatedShowId = showDao.getIdOrSavePlaceholder(show)
            // Make a copy of the entry with the id
            entry.copy(showId = showId, otherShowId = relatedShowId)
        }.also { entries ->
            // Save the related entriesWithShows
            relatedShowsStore.saveRelatedShows(showId, entries)
            // Now update all of the related shows if needed
            entries.parallelForEach { entry ->
                showStore.fetch(entry.showId)
                showImagesStore.fetchCollection(entry.showId)
            }
        }
    }
}
