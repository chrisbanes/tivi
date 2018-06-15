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

package app.tivi

import app.tivi.data.daos.EpisodesDao
import app.tivi.tmdb.TmdbEpisodeFetcher
import app.tivi.trakt.TraktEpisodeFetcher
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlinx.coroutines.experimental.withContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeFetcher @Inject constructor(
    private val episodeDao: EpisodesDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val traktEpisodeFetcher: TraktEpisodeFetcher,
    private val tmdbEpisodeFetcher: TmdbEpisodeFetcher,
    private val logger: Logger
) {
    suspend fun update(episodeId: Long, forceRefresh: Boolean = false) {
        val show = withContext(dispatchers.io) {
            episodeDao.episodeWithId(episodeId)!!
        }
        if (forceRefresh || show.needsUpdateFromTrakt()) {
            try {
                traktEpisodeFetcher.updateEpisodeData(episodeId)
            } catch (e: HttpException) {
                logger.e("Error while fetching episode data from Trakt", e)
            }
        }
        if (forceRefresh || show.needsUpdateFromTmdb()) {
            try {
                tmdbEpisodeFetcher.updateEpisodeData(episodeId)
            } catch (e: HttpException) {
                logger.e("Error while fetching episode data from TMDb", e)
            }
        }
    }
}