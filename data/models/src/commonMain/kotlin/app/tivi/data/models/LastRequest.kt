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
