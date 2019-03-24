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
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.Request
import app.tivi.data.entities.TiviShow
import io.reactivex.Flowable
import org.threeten.bp.temporal.TemporalAmount
import javax.inject.Inject

class LocalShowStore @Inject constructor(
    private val entityInserter: EntityInserter,
    private val showDao: TiviShowDao,
    private val lastRequestDao: LastRequestDao,
    private val transactionRunner: DatabaseTransactionRunner
) {
    suspend fun getShow(showId: Long) = showDao.getShowWithId(showId)

    fun observeShow(showId: Long): Flowable<TiviShow> = showDao.getShowWithIdFlowable(showId)

    suspend fun getIdForTraktId(traktId: Int) = showDao.getIdForTraktId(traktId)

    suspend fun saveShow(show: TiviShow) = entityInserter.insertOrUpdate(showDao, show)

    suspend fun lastRequestBefore(showId: Long, threshold: TemporalAmount): Boolean {
        return lastRequestDao.isRequestBefore(Request.SHOW_DETAILS, showId, threshold)
    }

    suspend fun updateLastRequest(showId: Long) = lastRequestDao.updateLastRequest(Request.SHOW_DETAILS, showId)

    /**
     * Gets the ID for the show with the given trakt Id. If the trakt Id does not exist in the
     * database, it is inserted and the generated ID is returned.
     */
    suspend fun getIdOrSavePlaceholder(show: TiviShow): Long = transactionRunner {
        show.traktId?.let { showDao.getShowWithTraktId(it)?.id }
                ?: show.tmdbId?.let { showDao.getShowWithTmdbId(it)?.id }
                ?: showDao.insert(show)
    }
}