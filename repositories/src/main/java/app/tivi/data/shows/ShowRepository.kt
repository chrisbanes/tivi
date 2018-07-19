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

package app.tivi.data.shows

import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.copyDynamic
import app.tivi.data.resultentities.RelatedShowEntryWithShow
import app.tivi.extensions.parallelForEach
import javax.inject.Inject

class ShowRepository @Inject constructor(
    private val localShowStore: LocalShowStore,
    private val tmdbShowDataSource: TmdbShowDataSource,
    private val traktShowDataSource: TraktShowDataSource
) {
    fun observeShow(showId: Long) = localShowStore.observeShow(showId)

    /**
     * Updates the show with the given id from all network sources, saves the result to the database
     */
    fun getShow(showId: Long) = localShowStore.getShow(showId)

    /**
     * Updates the show with the given id from all network sources, saves the result to the database
     */
    suspend fun updateShow(showId: Long) {
        val traktResult = traktShowDataSource.getShow(showId)
        val tmdbResult = tmdbShowDataSource.getShow(showId)
        val localResult = localShowStore.getShow(showId) ?: TiviShow.EMPTY_SHOW

        val show = mergeShow(localResult, traktResult, tmdbResult)
        localShowStore.saveShow(show)
    }

    fun observeRelatedShows(showId: Long) = localShowStore.observeRelatedShows(showId)

    fun getRelatedShows(showId: Long) = localShowStore.getRelatedShows(showId)

    suspend fun updateRelatedShows(showId: Long) {
        traktShowDataSource.getRelatedShows(showId)
                .map {
                    // Grab the show id if it exists, or save the show and use it's generated ID
                    val relatedShowId = localShowStore.getIdForTraktId(it.show.traktId!!)
                            ?: localShowStore.saveShow(it.show)
                    // Make a copy of the entry with the id
                    it.entry!!.copy(otherShowId = relatedShowId)
                }
                .also {
                    // Save the related entries
                    localShowStore.saveRelatedShows(showId, it)
                    // Now update all of the related shows if needed
                    it.parallelForEach { updateShow(it.otherShowId) }
                }
    }

    private fun mergeShow(
        localResult: TiviShow = TiviShow.EMPTY_SHOW,
        traktResult: TiviShow = TiviShow.EMPTY_SHOW,
        tmdbResult: TiviShow = TiviShow.EMPTY_SHOW
    ) = localResult.copyDynamic {
        title = traktResult.title ?: title
        summary = traktResult.summary ?: summary
        homepage = traktResult.summary ?: summary
        rating = traktResult.rating ?: rating
        certification = traktResult.certification ?: certification
        runtime = traktResult.runtime ?: runtime
        country = traktResult.country ?: country
        firstAired = traktResult.firstAired ?: firstAired
        _genres = traktResult._genres ?: _genres

        // Trakt specific stuff
        traktId = traktResult.traktId ?: traktId

        // TMDb specific stuff
        tmdbId = tmdbResult.tmdbId ?: traktResult.tmdbId ?: tmdbId
        tmdbPosterPath = tmdbResult.tmdbPosterPath ?: tmdbPosterPath
        tmdbBackdropPath = tmdbResult.tmdbBackdropPath ?: tmdbBackdropPath
    }
}