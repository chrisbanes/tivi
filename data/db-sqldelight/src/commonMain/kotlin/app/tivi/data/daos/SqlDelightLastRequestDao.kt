/*
 * Copyright 2023 Google LLC
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

import app.tivi.data.Database
import app.tivi.data.models.LastRequest
import app.tivi.data.models.Request
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightLastRequestDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : LastRequestDao, SqlDelightEntityDao<LastRequest> {
    override fun lastRequest(
        request: Request,
        entityId: Long,
    ): LastRequest? {
        return db.last_requestsQueries.getLastRequestForId(request, entityId, ::LastRequest)
            .executeAsOneOrNull()
    }

    override fun requestCount(
        request: Request,
        entityId: Long,
    ): Int {
        return db.last_requestsQueries.requestCount(request, entityId)
            .executeAsOne().toInt()
    }

    override fun deleteEntity(entity: LastRequest) {
        db.last_requestsQueries.delete(entity.id)
    }

    override fun upsert(entity: LastRequest): Long {
        return db.last_requestsQueries.upsert(
            entity = entity,
            insert = {
                insert(
                    id = it.id,
                    entity_id = it.entityId,
                    request = it.request,
                    timestamp = it.timestamp,
                )
            },
            update = {
                update(
                    id = it.id,
                    entity_id = it.entityId,
                    request = it.request,
                    timestamp = it.timestamp,
                )
            },
            lastInsertRowId = { lastInsertRowId().executeAsOne() },
            onConflict = { e, throwable ->
                val id = db.last_requestsQueries.getLastRequestForId(
                    entity.request,
                    entity.entityId,
                ).executeAsOneOrNull()?.id

                if (id != null) {
                    update(
                        id = id,
                        entity_id = e.entityId,
                        request = e.request,
                        timestamp = e.timestamp,
                    )
                    id
                } else {
                    throw throwable
                }
            },
        )
    }
}
