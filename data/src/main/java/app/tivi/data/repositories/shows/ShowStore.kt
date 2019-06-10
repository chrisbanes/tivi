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
import app.tivi.data.daos.ShowImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.ShowDetailed
import io.reactivex.Observable
import javax.inject.Inject

class ShowStore @Inject constructor(
    private val entityInserter: EntityInserter,
    private val showDao: TiviShowDao,
    private val showImagesDao: ShowImagesDao,
    private val transactionRunner: DatabaseTransactionRunner
) {
    suspend fun getShow(showId: Long) = showDao.getShowWithId(showId)

    suspend fun getShowDetailed(showId: Long) = showDao.getShowWithIdDetailed(showId)

    fun observeShowDetailed(showId: Long): Observable<ShowDetailed> = showDao.getShowWithIdObservable(showId)

    suspend fun saveShow(show: TiviShow) = entityInserter.insertOrUpdate(showDao, show)

    /**
     * Gets the ID for the show with the given trakt Id. If the trakt Id does not exist in the
     * database, it is inserted and the generated ID is returned.
     */
    suspend fun getIdOrSavePlaceholder(show: TiviShow): Long = transactionRunner {
        show.traktId?.let { showDao.getShowWithTraktId(it)?.id }
                ?: show.tmdbId?.let { showDao.getShowWithTmdbId(it)?.id }
                ?: showDao.insert(show)
    }

    suspend fun searchShows(query: String) = showDao.search("*$query*")

    suspend fun saveImages(showId: Long, images: List<ShowTmdbImage>) = transactionRunner {
        showImagesDao.deleteForShowId(showId)
        entityInserter.insertOrUpdate(showImagesDao, images)
    }
}