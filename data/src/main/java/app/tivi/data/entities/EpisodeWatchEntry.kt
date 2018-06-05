/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.data.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "episode_watch_entries",
        indices = [
            Index(value = ["episode_id"]),
            Index(value = ["trakt_id"], unique = true)
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Episode::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("episode_id"),
                    onUpdate = ForeignKey.CASCADE,
                    onDelete = ForeignKey.CASCADE
            )
        ]
)
data class EpisodeWatchEntry(
    @PrimaryKey(autoGenerate = true) override val id: Long? = null,
    @ColumnInfo(name = "episode_id") val episodeId: Long,
    @ColumnInfo(name = "trakt_id") val traktId: Long,
    @ColumnInfo(name = "watched_at") val watchedAt: OffsetDateTime,
    @ColumnInfo(name = "pending_action") val pendingAction: Int
) : TiviEntity {
    companion object {
        const val PENDING_ACTION_NOTHING = 0
        const val PENDING_ACTION_SEND_TRAKT = 1
        const val PENDING_ACTION_DELETE_FROM_TRAKT = 2
    }
}