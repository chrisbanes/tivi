// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.Database
import app.tivi.data.models.LastRequest
import app.tivi.data.models.Request
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightLastRequestDao(
    override val db: Database,
) : LastRequestDao, SqlDelightEntityDao<LastRequest> {
    override fun insert(entity: LastRequest): Long {
        db.last_requestsQueries.insert(
            id = entity.id,
            entity_id = entity.entityId,
            request = entity.request,
            timestamp = entity.timestamp,
        )
        return db.last_requestsQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: LastRequest) {
        db.last_requestsQueries.update(
            id = entity.id,
            entity_id = entity.entityId,
            request = entity.request,
            timestamp = entity.timestamp,
        )
    }

    override fun upsert(entity: LastRequest): Long = upsert(
        entity = entity,
        insert = ::insert,
        update = ::update,
        onConflict = { e, throwable ->
            val id = db.last_requestsQueries.getLastRequestForId(
                entity.request,
                entity.entityId,
            ).executeAsOneOrNull()?.id

            if (id != null) {
                db.last_requestsQueries.update(
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
}
