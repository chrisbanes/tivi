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

package app.tivi.data.repositories.shows

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.ShowFtsDao
import app.tivi.data.daos.ShowImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import javax.inject.Inject

class ShowStore @Inject constructor(
    private val entityInserter: EntityInserter,
    private val showDao: TiviShowDao,
    private val showFtsDao: ShowFtsDao,
    private val showImagesDao: ShowImagesDao,
    private val transactionRunner: DatabaseTransactionRunner
) {
    suspend fun getShow(showId: Long) = showDao.getShowWithId(showId)

    suspend fun getShowOrEmpty(showId: Long) = showDao.getShowWithId(showId) ?: TiviShow.EMPTY_SHOW

    suspend fun getShowDetailed(showId: Long) = showDao.getShowWithIdDetailed(showId)

    fun observeShowDetailed(showId: Long) = showDao.getShowWithIdFlow(showId)

    suspend fun saveShow(show: TiviShow) = entityInserter.insertOrUpdate(showDao, show)

    suspend fun updateShowFromSources(showId: Long, trakt: TiviShow) = transactionRunner {
        val localShow = getShowOrEmpty(showId)
        val merged = mergeShows(localShow, trakt)

        if (localShow != merged) {
            saveShow(merged)
        }
    }

    /**
     * Gets the ID for the show with the given trakt Id. If the trakt Id does not exist in the
     * database, it is inserted and the generated ID is returned.
     */
    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    suspend fun getIdOrSavePlaceholder(show: TiviShow): Long = transactionRunner {
        val idForTraktId: Long? = if (show.traktId != null) showDao.getIdForTraktId(show.traktId) else null
        val idForTmdbId: Long? = if (show.tmdbId != null) showDao.getIdForTmdbId(show.tmdbId) else null

        if (idForTraktId != null && idForTmdbId != null) {
            if (idForTmdbId == idForTraktId) {
                // Great, the entities are matching
                return@transactionRunner idForTraktId!!
            } else {
                val showForTmdbId = showDao.getShowWithId(idForTmdbId)!!
                val showForTraktId = showDao.getShowWithId(idForTraktId)!!

                showDao.delete(showForTmdbId)
                return@transactionRunner saveShow(
                        mergeShows(showForTraktId, showForTraktId, showForTmdbId)
                )
            }
        }
        if (idForTraktId != null) {
            // If we get here, we only have a entity with the trakt id
            return@transactionRunner idForTraktId!!
        }
        if (idForTmdbId != null) {
            // If we get here, we only have a entity with the tmdb id
            return@transactionRunner idForTmdbId!!
        }

        // TODO add fuzzy search on name or slug

        showDao.insert(show)
    }

    suspend fun searchShows(query: String) = showFtsDao.search("*$query*")

    suspend fun saveImages(showId: Long, images: List<ShowTmdbImage>) = transactionRunner {
        showImagesDao.deleteForShowId(showId)
        entityInserter.insertOrUpdate(showImagesDao, images)
    }

    private fun mergeShows(
        local: TiviShow,
        trakt: TiviShow = TiviShow.EMPTY_SHOW,
        tmdb: TiviShow = TiviShow.EMPTY_SHOW
    ) = local.copy(
            title = trakt.title ?: local.title,
            summary = trakt.summary ?: local.summary,
            homepage = trakt.homepage ?: local.homepage,
            network = trakt.network ?: local.network,
            certification = trakt.certification ?: local.certification,
            runtime = trakt.runtime ?: local.runtime,
            country = trakt.country ?: local.country,
            firstAired = trakt.firstAired ?: local.firstAired,
            _genres = trakt._genres ?: local._genres,
            status = trakt.status ?: local.status,

            // Trakt specific stuff
            traktId = trakt.traktId ?: local.traktId,
            traktRating = trakt.traktRating ?: local.traktRating,
            traktDataUpdate = trakt.traktDataUpdate ?: local.traktDataUpdate,

            // TMDb specific stuff
            tmdbId = tmdb.tmdbId ?: trakt.tmdbId ?: local.tmdbId
    )
}