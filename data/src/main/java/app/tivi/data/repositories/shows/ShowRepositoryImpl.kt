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

package app.tivi.data.repositories.shows

import app.tivi.data.entities.TiviShow
import app.tivi.inject.Tmdb
import app.tivi.inject.Trakt
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.experimental.async
import javax.inject.Inject

class ShowRepositoryImpl @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val localShowStore: LocalShowStore,
    @Tmdb private val tmdbShowDataSource: ShowDataSource,
    @Trakt private val traktShowDataSource: ShowDataSource
) : ShowRepository {
    override fun observeShow(showId: Long) = localShowStore.observeShow(showId)

    /**
     * Updates the show with the given id from all network sources, saves the result to the database
     */
    override suspend fun getShow(showId: Long): TiviShow {
        updateShow(showId)
        return localShowStore.getShow(showId)!!
    }

    /**
     * Updates the show with the given id from all network sources, saves the result to the database
     */
    override suspend fun updateShow(showId: Long) {
        val traktResult = async(dispatchers.io) {
            traktShowDataSource.getShow(showId) ?: TiviShow.EMPTY_SHOW
        }
        val tmdbResult = async(dispatchers.io) {
            tmdbShowDataSource.getShow(showId) ?: TiviShow.EMPTY_SHOW
        }
        val localResult = async(dispatchers.io) {
            localShowStore.getShow(showId) ?: TiviShow.EMPTY_SHOW
        }
        localShowStore.saveShow(mergeShow(localResult.await(), traktResult.await(), tmdbResult.await()))
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

            // TMDb specific stuff
            tmdbId = tmdb.tmdbId ?: trakt.tmdbId ?: local.tmdbId,
            tmdbPosterPath = tmdb.tmdbPosterPath ?: local.tmdbPosterPath,
            tmdbBackdropPath = tmdb.tmdbBackdropPath ?: local.tmdbBackdropPath
    )
}