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

import app.tivi.data.daos.LastRequestDao
import app.tivi.data.entities.Request.EPISODE_DETAILS
import app.tivi.tmdb.TmdbEpisodeFetcher
import app.tivi.trakt.TraktEpisodeFetcher
import app.tivi.util.Logger
import org.threeten.bp.Period
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeFetcher @Inject constructor(
    private val lastRequests: LastRequestDao,
    private val traktEpisodeFetcher: TraktEpisodeFetcher,
    private val tmdbEpisodeFetcher: TmdbEpisodeFetcher,
    private val logger: Logger
) {
    suspend fun updateIfOld(episodeId: Long) {
        if (lastRequests.isRequestBefore(EPISODE_DETAILS, episodeId, Period.ofDays(1))) {
            update(episodeId)
        }
    }

    suspend fun update(episodeId: Long) {
        try {
            tmdbEpisodeFetcher.updateEpisodeData(episodeId)
            traktEpisodeFetcher.updateEpisodeData(episodeId)

            lastRequests.updateLastRequest(EPISODE_DETAILS, episodeId)
        } catch (e: HttpException) {
            logger.e("Error while fetching episode data from Trakt", e)
        }
    }
}