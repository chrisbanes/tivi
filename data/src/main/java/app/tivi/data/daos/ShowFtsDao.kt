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
import app.tivi.data.resultentities.ShowDetailed

@Dao
abstract class ShowFtsDao {
    @Transaction
    @Query(
        """
        SELECT s.* FROM shows as s
        INNER JOIN shows_fts AS fts ON s.id = fts.docid
        WHERE fts.title MATCH :filter
        """
    )
    abstract suspend fun search(filter: String): List<ShowDetailed>
}
