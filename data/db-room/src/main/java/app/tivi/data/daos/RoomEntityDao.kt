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

package app.tivi.data.daos

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Transaction
import androidx.room.Update
import app.tivi.data.models.TiviEntity

interface RoomEntityDao<in E : TiviEntity> : EntityDao<E> {
    @Insert
    override suspend fun insert(entity: E): Long

    @Insert
    override suspend fun insertAll(vararg entity: E)

    @Insert
    override suspend fun insertAll(entities: List<E>)

    @Update
    override suspend fun update(entity: E)

    @Delete
    override suspend fun deleteEntity(entity: E): Int

    @Transaction
    override suspend fun withTransaction(tx: suspend () -> Unit) = tx()
}
