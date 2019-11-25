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

package app.tivi.data.repositories.lastrequests

import app.tivi.data.daos.LastRequestDao
import app.tivi.data.entities.LastRequest
import app.tivi.data.entities.Request
import app.tivi.data.inPast
import org.threeten.bp.Instant
import org.threeten.bp.temporal.TemporalAmount

open class GroupLastRequestStore(
    private val request: Request,
    private val dao: LastRequestDao
) {
    suspend fun getRequestInstant(): Instant? {
        return dao.lastRequest(request, DEFAULT_ID)?.timestamp
    }

    suspend fun isRequestExpired(threshold: TemporalAmount): Boolean {
        return isRequestBefore(threshold.inPast())
    }

    suspend fun isRequestBefore(instant: Instant): Boolean {
        return getRequestInstant()?.isBefore(instant) ?: true
    }

    suspend fun updateLastRequest(timestamp: Instant = Instant.now()) {
        dao.insert(LastRequest(request = request, entityId = DEFAULT_ID, timestamp = timestamp))
    }

    suspend fun invalidateLastRequest() = updateLastRequest(Instant.EPOCH)

    companion object {
        private const val DEFAULT_ID = 0L
    }
}
