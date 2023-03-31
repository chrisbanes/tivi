/*
 * Copyright 2019 Google LLC
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

package app.tivi.data.daos

import app.tivi.data.models.ShowTmdbImage
import kotlinx.coroutines.flow.Flow

interface ShowTmdbImagesDao : EntityDao<ShowTmdbImage> {

    suspend fun deleteForShowId(showId: Long)

    suspend fun imageCountForShowId(showId: Long): Int

    fun getImagesForShowId(showId: Long): Flow<List<ShowTmdbImage>>

    suspend fun deleteAll()
}

suspend fun ShowTmdbImagesDao.saveImages(showId: Long, images: List<ShowTmdbImage>) {
    deleteForShowId(showId)
    upsertAll(images)
}

suspend fun ShowTmdbImagesDao.saveImagesIfEmpty(showId: Long, images: List<ShowTmdbImage>) {
    if (imageCountForShowId(showId) <= 0) {
        upsertAll(images)
    }
}
