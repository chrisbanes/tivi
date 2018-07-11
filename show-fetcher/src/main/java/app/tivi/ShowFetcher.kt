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
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.Request.SHOW_DETAILS
import app.tivi.tmdb.TmdbShowFetcher
import app.tivi.trakt.TraktShowFetcher
import com.uwetrottmann.trakt5.entities.Show
import org.threeten.bp.Period
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowFetcher @Inject constructor(
    private val showDao: TiviShowDao,
    private val lastRequests: LastRequestDao,
    private val traktShowFetcher: TraktShowFetcher,
    private val tmdbShowFetcher: TmdbShowFetcher
) {
    suspend fun insertPlaceholderIfNeeded(show: Show): Long {
        return traktShowFetcher.insertPlaceholderIfNeeded(show)
    }

    suspend fun updateIfNeeded(showId: Long) {
        if (lastRequests.isRequestBefore(SHOW_DETAILS, showId, Period.ofDays(1))) {
            update(showId)
        }
    }

    suspend fun update(showId: Long) {
        val show = showDao.getShowWithId(showId)!!

        traktShowFetcher.updateShow(show.traktId!!)
        tmdbShowFetcher.updateShow(show.tmdbId!!)

        lastRequests.updateLastRequest(SHOW_DETAILS, showId)
    }
}