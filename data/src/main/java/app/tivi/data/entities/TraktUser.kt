/*
 * Copyright 2017 Google LLC
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
import androidx.room.Index
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class TraktUser(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") override val id: Long = 0,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "joined_date") val joined: OffsetDateTime? = null,
    @ColumnInfo(name = "location") val location: String? = null,
    @ColumnInfo(name = "about") val about: String? = null,
    @ColumnInfo(name = "avatar_url") val avatarUrl: String? = null,
    @ColumnInfo(name = "vip") val vip: Boolean? = null,
    @ColumnInfo(name = "is_me") val isMe: Boolean = false
) : TiviEntity
