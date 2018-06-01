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
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpisodeFetcher @Inject constructor(
    private val episodeDao: EpisodesDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val traktEpisodeFetcher: TraktEpisodeFetcher,
    private val tmdbEpisodeFetcher: TmdbEpisodeFetcher
) {
    suspend fun update(episodeId: Long) {
        val show = withContext(dispatchers.database) {
            episodeDao.episodeWithId(episodeId)!!
        }

        val traktJob = if (show.needsUpdateFromTrakt()) {
            async { traktEpisodeFetcher.updateEpisodeData(episodeId) }
        } else {
            null
        }

        val tmdbJob = if (show.needsUpdateFromTmdb()) {
            async { tmdbEpisodeFetcher.updateEpisodeData(episodeId) }
        } else {
            null
        }

        traktJob?.await()
        tmdbJob?.await()
    }
}