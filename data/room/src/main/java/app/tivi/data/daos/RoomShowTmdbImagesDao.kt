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
import app.tivi.data.models.ShowTmdbImage
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RoomShowTmdbImagesDao : ShowTmdbImagesDao, RoomEntityDao<ShowTmdbImage> {
    @Query("DELETE FROM show_images WHERE show_id = :showId")
    abstract override suspend fun deleteForShowId(showId: Long)

    @Query("SELECT COUNT(*) FROM show_images WHERE show_id = :showId")
    abstract override suspend fun imageCountForShowId(showId: Long): Int

    @Query("SELECT * FROM show_images WHERE show_id = :showId")
    abstract override fun getImagesForShowId(showId: Long): Flow<List<ShowTmdbImage>>

    @Query("DELETE FROM show_images")
    abstract override suspend fun deleteAll()
}
