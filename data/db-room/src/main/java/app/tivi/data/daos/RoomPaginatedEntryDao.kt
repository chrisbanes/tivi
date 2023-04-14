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
import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.models.PaginatedEntry

interface RoomPaginatedEntryDao<EC : PaginatedEntry, LI : EntryWithShow<EC>> :
    RoomEntryDao<EC, LI>,
    PaginatedEntryDao<EC, LI> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsert(entity: EC): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun upsertAll(entities: List<EC>)
}
