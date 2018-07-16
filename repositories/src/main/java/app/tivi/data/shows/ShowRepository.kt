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
) : ShowStore, ShowDataSource {

    override fun observeShow(showId: Long) = localShowStore.observeShow(showId)

    /**
     * Updates the show with the given id from all network sources, saves the result to the database
     */
    override suspend fun getShow(showId: Long): TiviShow {
        val traktResult = traktShowDataSource.getShow(showId)
        val tmdbResult = tmdbShowDataSource.getShow(showId)
        val localResult = localShowStore.getShow(showId) ?: TiviShow()

        return mergeShow(localResult, traktResult, tmdbResult)
                .also { localShowStore.saveShow(it) }
    }

    override fun observeRelatedShows(showId: Long) = localShowStore.observeRelatedShows(showId)

    override suspend fun getRelatedShows(showId: Long): List<RelatedShowEntryWithShow> {
        val localResult = localShowStore.getRelatedShows(showId)
        if (localResult.isNotEmpty()) {
            return localResult
        }

        return traktShowDataSource.getRelatedShows(showId)
                .map {
                    val relatedShowId = localShowStore.getIdForTraktId(it.show.traktId!!)
                            ?: localShowStore.saveShow(it.show)
                    it.entry!!.copy(otherShowId = relatedShowId)
                }
                .also {
                    // Save the related entries
                    localShowStore.saveRelatedShows(showId, it)
                    // Now update all of the related shows if needed
                    it.parallelForEach {
                        getShow(it.otherShowId)
                    }
                }
                .let {
                    localShowStore.getRelatedShows(showId)
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