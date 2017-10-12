/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.data.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import me.banes.chris.tivi.data.PaginatedEntry

@Entity(tableName = "trending_shows",
        indices = arrayOf(Index(value = "show_id", unique = true)),
        foreignKeys = arrayOf(
                ForeignKey(entity = TiviShow::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("show_id"),
                        onUpdate = ForeignKey.CASCADE,
                        onDelete = ForeignKey.CASCADE)))
data class TrendingEntry(
        @PrimaryKey(autoGenerate = true) override val id: Long? = null,
        @ColumnInfo(name = "show_id") override val showId: Long,
        @ColumnInfo(name = "page") override val page: Int,
        @ColumnInfo(name = "page_order") override val pageOrder: Int
) : PaginatedEntry {
    @Ignore override var show : TiviShow? = null
}