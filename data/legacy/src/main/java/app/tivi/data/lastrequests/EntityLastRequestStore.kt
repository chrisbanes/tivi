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
    private suspend fun getRequestInstant(entityId: Long): Instant? {
        return dao.lastRequest(request, entityId)?.timestamp
    }

    suspend fun isRequestExpired(entityId: Long, threshold: Duration): Boolean {
        return isRequestBefore(entityId, Clock.System.now() - threshold)
    }

    suspend fun hasBeenRequested(entityId: Long): Boolean = dao.requestCount(request, entityId) > 0

    suspend fun isRequestBefore(entityId: Long, instant: Instant): Boolean {
        return getRequestInstant(entityId)?.let { it < instant } ?: true
    }

    suspend fun updateLastRequest(entityId: Long, timestamp: Instant = Clock.System.now()) {
        dao.upsert(
            LastRequest(
                request = request,
                entityId = entityId,
                _timestamp = timestamp.toEpochMilliseconds(),
            ),
        )
    }

    private suspend fun invalidateLastRequest(entityId: Long) = updateLastRequest(entityId, Instant.DISTANT_PAST)
}
