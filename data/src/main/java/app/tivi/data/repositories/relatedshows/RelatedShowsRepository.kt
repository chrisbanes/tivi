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

import app.tivi.data.repositories.shows.LocalShowStore
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.extensions.parallelForEach
import app.tivi.util.AppCoroutineDispatchers
import javax.inject.Inject

class RelatedShowsRepository @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val localStore: LocalRelatedShowsStore,
    private val localShowStore: LocalShowStore,
    private val traktDataSource: TraktRelatedShowsDataSource,
    private val showRepository: ShowRepository
) {
    fun observeRelatedShows(showId: Long) = localStore.observeRelatedShows(showId)

    suspend fun getRelatedShows(showId: Long) {
        updateRelatedShows(showId)
        localStore.getRelatedShows(showId)
    }

    suspend fun updateRelatedShows(showId: Long) {
        traktDataSource.getRelatedShows(showId)
                .map { (show, entry) ->
                    // Grab the show id if it exists, or save the show and use it's generated ID
                    val relatedShowId = localShowStore.getIdOrSavePlaceholder(show)
                    // Make a copy of the entry with the id
                    entry.copy(otherShowId = relatedShowId)
                }
                .also {
                    // Save the related entries
                    localStore.saveRelatedShows(showId, it)
                    // Now update all of the related shows if needed
                    it.parallelForEach(dispatchers.io) {
                        if (showRepository.needsUpdate(it.otherShowId)) {
                            showRepository.updateShow(it.otherShowId)
                        }
                    }
                }
    }
}