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

package app.tivi.data

import android.database.sqlite.SQLiteException
import app.tivi.data.daos.EntityDao
import app.tivi.data.daos.EntityInserter
import app.tivi.data.entities.TiviEntity
import app.tivi.util.Logger
import javax.inject.Inject

class TiviEntityInserter @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val logger: Logger
) : EntityInserter {
    override suspend fun <E : TiviEntity> insertOrUpdate(dao: EntityDao<E>, entities: List<E>) {
        transactionRunner {
            entities.forEach {
                insertOrUpdate(dao, it)
            }
        }
    }

    override suspend fun <E : TiviEntity> insertOrUpdate(dao: EntityDao<E>, entity: E): Long {
        logger.d("insertOrUpdate: %s", entity)

        return if (entity.id == 0L) {
            try {
                dao.insert(entity)
            } catch (e: SQLiteException) {
                throw SQLiteException("Error while inserting entity: $entity").apply {
                    initCause(e)
                }
            }
        } else {
            try {
                dao.update(entity)
                entity.id
            } catch (e: SQLiteException) {
                throw SQLiteException("Error while updating entity: $entity").apply {
                    initCause(e)
                }
            }
        }
    }
}