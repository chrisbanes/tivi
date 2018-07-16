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

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.RelatedShowEntry
import app.tivi.data.entities.TiviShow
import io.reactivex.Flowable
import javax.inject.Inject

class LocalShowStore @Inject constructor(
    private val entityInserter: EntityInserter,
    private val transactionRunner: DatabaseTransactionRunner,
    private val showDao: TiviShowDao,
    private val relatedShowsDao: RelatedShowsDao
) : ShowStore {
    override suspend fun getShow(showId: Long) = showDao.getShowWithId(showId)

    override fun observeShow(showId: Long): Flowable<TiviShow> = showDao.getShowWithIdFlowable(showId)

    fun getIdForTraktId(traktId: Int) = showDao.getIdForTraktId(traktId)

    fun saveShow(show: TiviShow) = entityInserter.insertOrUpdate(showDao, show)

    override suspend fun getRelatedShows(showId: Long) = relatedShowsDao.entries(showId)

    override fun observeRelatedShows(showId: Long) = relatedShowsDao.entriesFlowable(showId)

    fun saveRelatedShows(showId: Long, relatedShows: List<RelatedShowEntry>) {
        if (relatedShows.isNotEmpty()) {
            transactionRunner.runInTransaction {
                relatedShowsDao.deleteWithShowId(showId)
                entityInserter.insertOrUpdate(relatedShowsDao, relatedShows)
            }
        }
    }
}