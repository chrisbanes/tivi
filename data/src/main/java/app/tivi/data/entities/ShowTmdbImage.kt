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

package app.tivi.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "show_images",
    indices = [
        Index(value = ["show_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TiviShow::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("show_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ShowTmdbImage(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") override val id: Long = 0,
    @ColumnInfo(name = "show_id") val showId: Long,
    @ColumnInfo(name = "path") override val path: String,
    @ColumnInfo(name = "type") override val type: ImageType,
    @ColumnInfo(name = "lang") override val language: String? = null,
    @ColumnInfo(name = "rating") override val rating: Float = 0f,
    @ColumnInfo(name = "is_primary") override val isPrimary: Boolean = false
) : TiviEntity, TmdbImageEntity
