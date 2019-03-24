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

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.entities.TiviEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntityInserter @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner
) {
    suspend fun <E : TiviEntity> insertOrUpdate(dao: EntityDao<E>, entities: List<E>) = transactionRunner {
        entities.forEach {
            insertOrUpdate(dao, it)
        }
    }

    suspend fun <E : TiviEntity> insertOrUpdate(dao: EntityDao<E>, entity: E): Long = when {
        entity.id == 0L -> dao.insert(entity)
        else -> {
            dao.update(entity)
            entity.id
        }
    }
}