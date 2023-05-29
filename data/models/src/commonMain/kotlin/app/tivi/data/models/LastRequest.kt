// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

import kotlinx.datetime.Instant

data class LastRequest(
    override val id: Long = 0,
    val request: Request,
    val entityId: Long,
    // We have to use a raw Long type here rather than Timestamp. This is because Timestamp is
    // currently mapped to a string (by type converters) for legacy reasons. For the same reason,
    // the old Instant type converter mapped to an int sql type, meaning that we can use the
    // same type converter for pre-existing data.
    internal val _timestamp: Long,
) : TiviEntity {
    constructor(
        id: Long = 0,
        request: Request,
        entityId: Long,
        timestamp: Instant,
    ) : this(id, request, entityId, timestamp.toEpochMilliseconds())

    val timestamp: Instant by lazy { Instant.fromEpochMilliseconds(_timestamp) }
}
