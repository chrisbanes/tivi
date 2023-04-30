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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(
    tableName = "last_requests",
    indices = [Index(value = ["request", "entity_id"], unique = true)],
)
data class LastRequest(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    override val id: Long = 0,
    @ColumnInfo(name = "request") val request: Request,
    @ColumnInfo(name = "entity_id") val entityId: Long,
    // We have to use a raw Long type here rather than Timestamp. This is because Timestamp is
    // currently mapped to a string (by type converters) for legacy reasons. For the same reason,
    // the old Instant type converter mapped to an int sql type, meaning that we can use the
    // same type converter for pre-existing data.
    @ColumnInfo(name = "timestamp") internal val _timestamp: Long,
) : TiviEntity {
    constructor(
        id: Long = 0,
        request: Request,
        entityId: Long,
        timestamp: Instant,
    ) : this(id, request, entityId, timestamp.toEpochMilliseconds())

    @delegate:Ignore
    val timestamp: Instant by lazy { Instant.fromEpochMilliseconds(_timestamp) }
}
