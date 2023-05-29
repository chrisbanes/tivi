// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.lastrequests

import app.tivi.data.daos.LastRequestDao
import app.tivi.data.models.LastRequest
import app.tivi.data.models.Request
import kotlin.time.Duration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

abstract class EntityLastRequestStore(
    private val request: Request,
    private val dao: LastRequestDao,
) {
    private fun getRequestInstant(entityId: Long): Instant? {
        return dao.lastRequest(request, entityId)?.timestamp
    }

    fun isRequestExpired(entityId: Long, threshold: Duration): Boolean {
        return isRequestBefore(entityId, Clock.System.now() - threshold)
    }

    fun hasBeenRequested(entityId: Long): Boolean = dao.requestCount(request, entityId) > 0

    fun isRequestBefore(entityId: Long, instant: Instant): Boolean {
        return getRequestInstant(entityId)?.let { it < instant } ?: true
    }

    fun updateLastRequest(entityId: Long, timestamp: Instant = Clock.System.now()) {
        dao.upsert(
            LastRequest(
                request = request,
                entityId = entityId,
                _timestamp = timestamp.toEpochMilliseconds(),
            ),
        )
    }

    private fun invalidateLastRequest(entityId: Long) = updateLastRequest(entityId, Instant.DISTANT_PAST)
}
