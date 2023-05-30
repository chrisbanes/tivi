// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.lastrequests

import app.tivi.data.daos.LastRequestDao
import app.tivi.data.models.LastRequest
import app.tivi.data.models.Request
import kotlin.time.Duration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

open class GroupLastRequestStore(
    private val request: Request,
    private val dao: LastRequestDao,
) {
    fun getRequestInstant(): Instant? {
        return dao.lastRequest(request, DEFAULT_ID)?.timestamp
    }

    fun isRequestExpired(threshold: Duration): Boolean {
        return isRequestBefore(Clock.System.now() - threshold)
    }

    fun isRequestValid(threshold: Duration): Boolean {
        return !isRequestExpired(threshold)
    }

    fun isRequestBefore(instant: Instant): Boolean {
        return getRequestInstant()?.let { it < instant } ?: true
    }

    fun updateLastRequest(timestamp: Instant = Clock.System.now()) {
        dao.upsert(
            LastRequest(
                request = request,
                entityId = DEFAULT_ID,
                _timestamp = timestamp.toEpochMilliseconds(),
            ),
        )
    }

    fun invalidateLastRequest() = updateLastRequest(Instant.DISTANT_PAST)

    companion object {
        private const val DEFAULT_ID = 0L
    }
}
