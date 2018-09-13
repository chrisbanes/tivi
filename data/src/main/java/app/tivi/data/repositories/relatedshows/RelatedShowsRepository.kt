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

package app.tivi.data.repositories.relatedshows

import app.tivi.data.entities.RelatedShowEntry
import app.tivi.data.entities.Success
import app.tivi.data.entities.TiviShow
import app.tivi.data.repositories.shows.LocalShowStore
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.extensions.parallelForEach
import app.tivi.util.AppCoroutineDispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RelatedShowsRepository @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val localStore: LocalRelatedShowsStore,
    private val localShowStore: LocalShowStore,
    private val traktDataSource: TraktRelatedShowsDataSource,
    private val tmdbDataSource: TmdbRelatedShowsDataSource,
    private val showRepository: ShowRepository
) {
    fun observeRelatedShows(showId: Long) = localStore.observeRelatedShows(showId)

    suspend fun getRelatedShows(showId: Long) {
        updateRelatedShows(showId)
        localStore.getRelatedShows(showId)
    }

    suspend fun updateRelatedShows(showId: Long) {
        val tmdbResults = tmdbDataSource.getRelatedShows(showId)
        if (tmdbResults is Success && tmdbResults.data.isNotEmpty()) {
            process(showId, tmdbResults.data)
            return
        }

        val traktResults = traktDataSource.getRelatedShows(showId)
        if (traktResults is Success && traktResults.data.isNotEmpty()) {
            process(showId, traktResults.data)
            return
        }
    }

    private suspend fun process(showId: Long, list: List<Pair<TiviShow, RelatedShowEntry>>) {
        list.map { (show, entry) ->
            // Grab the show id if it exists, or save the show and use it's generated ID
            val relatedShowId = localShowStore.getIdOrSavePlaceholder(show)
            // Make a copy of the entry with the id
            entry.copy(showId = showId, otherShowId = relatedShowId)
        }.also { entries ->
            // Save the related entries
            localStore.saveRelatedShows(showId, entries)
            // Now update all of the related shows if needed
            entries.parallelForEach { entry ->
                if (showRepository.needsUpdate(entry.otherShowId)) {
                    showRepository.updateShow(entry.otherShowId)
                }
            }
        }
    }
}