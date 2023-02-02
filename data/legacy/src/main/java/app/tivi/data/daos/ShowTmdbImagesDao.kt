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

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.tivi.data.entities.ShowTmdbImage
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ShowTmdbImagesDao : EntityDao<ShowTmdbImage>() {
    @Query("DELETE FROM show_images WHERE show_id = :showId")
    abstract suspend fun deleteForShowId(showId: Long)

    @Query("SELECT COUNT(*) FROM show_images WHERE show_id = :showId")
    abstract suspend fun imageCountForShowId(showId: Long): Int

    @Query("SELECT * FROM show_images WHERE show_id = :showId")
    abstract fun getImagesForShowId(showId: Long): Flow<List<ShowTmdbImage>>

    @Query("DELETE FROM show_images")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun saveImages(showId: Long, images: List<ShowTmdbImage>) {
        deleteForShowId(showId)
        insertOrUpdate(images)
    }

    @Transaction
    open suspend fun saveImagesIfEmpty(showId: Long, images: List<ShowTmdbImage>) {
        if (imageCountForShowId(showId) <= 0) {
            insertAll(images)
        }
    }
}
