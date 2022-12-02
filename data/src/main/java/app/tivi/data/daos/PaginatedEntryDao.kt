/*
 * Copyright 2017 Google LLC
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

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import app.tivi.data.PaginatedEntry
import app.tivi.data.resultentities.EntryWithShow

abstract class PaginatedEntryDao<EC : PaginatedEntry, LI : EntryWithShow<EC>> : EntryDao<EC, LI>() {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(entity: EC): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insertAll(vararg entity: EC)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insertAll(entities: List<EC>)

    abstract suspend fun deletePage(page: Int)
    abstract suspend fun getLastPage(): Int?

    @Transaction
    open suspend fun updatePage(page: Int, entities: List<EC>) {
        deletePage(page)
        insertAll(entities)
    }
}
