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

package me.banes.chris.tivi

import com.uwetrottmann.trakt5.entities.Show
import kotlinx.coroutines.experimental.withContext
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.tmdb.TmdbShowFetcher
import me.banes.chris.tivi.trakt.TraktShowFetcher
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowFetcher @Inject constructor(
    private val showDao: TiviShowDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val traktShowFetcher: TraktShowFetcher,
    private val tmdbShowFetcher: TmdbShowFetcher
) {
    suspend fun insertPlaceholderIfNeeded(show: Show): Long {
        return traktShowFetcher.insertPlaceholderIfNeeded(show)
    }

    suspend fun update(showId: Long, force: Boolean = false) {
        val show = withContext(dispatchers.database) { showDao.getShowWithId(showId)!! }
        if (force || show.needsUpdateFromTrakt()) {
            traktShowFetcher.updateShow(show.traktId!!)
        }
        if (force || show.needsUpdateFromTmdb()) {
            tmdbShowFetcher.updateShow(show.tmdbId!!)
        }
    }
}