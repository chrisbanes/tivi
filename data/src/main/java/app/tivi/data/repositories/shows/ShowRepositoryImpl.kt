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

import app.tivi.data.entities.Success
import app.tivi.data.entities.TiviShow
import app.tivi.inject.Tmdb
import app.tivi.inject.Trakt
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowRepositoryImpl @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val showStore: ShowStore,
    private val showLastRequestStore: ShowLastRequestStore,
    @Tmdb private val tmdbShowDataSource: ShowDataSource,
    @Trakt private val traktShowDataSource: ShowDataSource
) : ShowRepository {
    override fun observeShow(showId: Long) = showStore.observeShow(showId)

    /**
     * Updates the show with the given id from all network sources, saves the result to the database
     */
    override suspend fun getShow(showId: Long): TiviShow {
        return showStore.getShow(showId)!!
    }

    /**
     * Updates the show with the given id from all network sources, saves the result to the database
     */
    override suspend fun updateShow(showId: Long) = coroutineScope {
        val localShow = showStore.getShow(showId) ?: TiviShow.EMPTY_SHOW
        val traktJob = async(dispatchers.io) {
            traktShowDataSource.getShow(localShow)
        }
        val tmdbJob = async(dispatchers.io) {
            tmdbShowDataSource.getShow(localShow)
        }

        val traktResult = traktJob.await()
        val tmdbResult = tmdbJob.await()

        val merged = mergeShow(localShow,
                traktResult.get() ?: TiviShow.EMPTY_SHOW,
                tmdbResult.get() ?: TiviShow.EMPTY_SHOW)
        showStore.saveShow(merged)

        if (tmdbResult is Success && traktResult is Success) {
            // If the network requests were successful, update the last request timestamp
            showLastRequestStore.updateLastRequest(showId)
        }
    }

    override suspend fun needsUpdate(showId: Long, expiry: Instant): Boolean {
        return showLastRequestStore.isRequestBefore(showId, expiry)
    }

    override suspend fun needsInitialUpdate(showId: Long): Boolean {
        return !showLastRequestStore.hasBeenRequested(showId)
    }

    override suspend fun getLastRequestInstant(showId: Long): Instant? {
        return showLastRequestStore.getRequestInstant(showId)
    }

    override suspend fun searchShows(query: String): List<TiviShow> {
        return if (query.isNotBlank()) {
            showStore.searchShows(query)
        } else {
            emptyList()
        }
    }

    private fun mergeShow(local: TiviShow, trakt: TiviShow, tmdb: TiviShow) = local.copy(
            title = trakt.title ?: local.title,
            summary = trakt.summary ?: local.summary,
            homepage = trakt.homepage ?: local.homepage,
            network = trakt.network ?: local.network,
            certification = trakt.certification ?: local.certification,
            runtime = trakt.runtime ?: local.runtime,
            country = trakt.country ?: local.country,
            firstAired = trakt.firstAired ?: local.firstAired,
            _genres = trakt._genres ?: local._genres,

            // Trakt specific stuff
            traktId = trakt.traktId ?: local.traktId,
            traktRating = trakt.traktRating ?: local.traktRating,
            traktDataUpdate = trakt.traktDataUpdate ?: local.traktDataUpdate,

            // TMDb specific stuff
            tmdbId = tmdb.tmdbId ?: trakt.tmdbId ?: local.tmdbId,
            tmdbPosterPath = tmdb.tmdbPosterPath ?: local.tmdbPosterPath,
            tmdbBackdropPath = tmdb.tmdbBackdropPath ?: local.tmdbBackdropPath
    )
}