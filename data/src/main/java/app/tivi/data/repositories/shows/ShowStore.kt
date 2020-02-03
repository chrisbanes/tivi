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

import app.tivi.data.daos.ShowFtsDao
import app.tivi.data.daos.ShowImagesDao
import app.tivi.data.daos.TiviShowDao
import javax.inject.Inject

class ShowStore @Inject constructor(
    private val showDao: TiviShowDao,
    private val showFtsDao: ShowFtsDao,
    private val showImagesDao: ShowImagesDao
) {
    suspend fun getShow(showId: Long) = showDao.getShowWithId(showId)

    fun observeShowDetailed(showId: Long) = showDao.getShowDetailedWithIdFlow(showId)

    suspend fun searchShows(query: String) = showFtsDao.search("*$query*")
}
